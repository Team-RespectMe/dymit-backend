"""Inflearn 사이트용 파싱 전략 클래스.

기능:
- listUrl + pagination 설정을 사용해 목록 페이지를 순회합니다.
- 목록에서 postId를 수집하고, 상세 페이지를 조회해 StudyInfo로 변환합니다.

매개변수:
- __init__(site_config): SiteConfigLoader 반환 딕셔너리의 sites 항목 중 하나
- apply(soup): 인터페이스 호환용 BeautifulSoup 매개변수
- load_root_page(page): 목록 페이지 번호
- load_detail(post_id): 상세 글 고유 ID

반환형:
- apply: list[StudyInfo]
- load_root_page: list[int]
- load_detail: StudyInfo
"""

from datetime import datetime, timedelta, timezone
import logging
import re
from typing import Any
from urllib.parse import parse_qsl, urlencode, urlparse, urlunparse

from bs4 import BeautifulSoup

from app.dto.study_info import StudyInfo
from app.simple_http.http_parser import extract_root_element_html
from app.simple_http.http_request import fetch_page_source
from app.strategy.strategy_interface import Strategy


logger = logging.getLogger(__name__)


class InflearnStrategy(Strategy):
    def __init__(self, site_config: dict[str, Any]) -> None:
        super().__init__(site_config)

    def _get_pagination(self) -> tuple[str, int, int]:
        pagination_raw = self.site_config.get("pagination", {})
        if isinstance(pagination_raw, list):
            pagination = pagination_raw[0] if pagination_raw else {}
        else:
            pagination = pagination_raw

        if not isinstance(pagination, dict):
            raise ValueError("pagination must be a dict or list containing one dict")

        page_param = str(pagination.get("pageParam", "page"))
        start_page = int(pagination.get("startPage", 1))
        end_page = int(pagination.get("endPage", start_page))
        return page_param, start_page, end_page

    def _build_list_url(self, page: int) -> str:
        list_url = str(self.site_config.get("listUrl", ""))
        if not list_url:
            raise ValueError("listUrl is required")

        page_param, _, _ = self._get_pagination()

        if "{}" in list_url:
            return list_url.format(page)

        parsed = urlparse(list_url)
        query = dict(parse_qsl(parsed.query, keep_blank_values=True))
        query[page_param] = str(page)
        new_query = urlencode(query)
        return urlunparse(parsed._replace(query=new_query))

    @staticmethod
    def _truncate_content(content: str, max_length: int = 100) -> str:
        if len(content) <= max_length:
            return content
        return content[: max_length - 3] + "..."

    @staticmethod
    def _to_utc_iso8601(created_raw: str) -> str:
        created_local = datetime.strptime(created_raw, "%y.%m.%d %H:%M")
        kst = timezone(timedelta(hours=9))
        created_utc = created_local.replace(tzinfo=kst).astimezone(timezone.utc)
        return created_utc.replace(microsecond=0).isoformat().replace("+00:00", "Z")

    def load_root_page(self, page: int) -> list[int]:
        list_url = self._build_list_url(page)
        logger.info("Loading root page: page=%s url=%s", page, list_url)
        html = fetch_page_source(list_url)
        root_html = extract_root_element_html(
            html,
            {"tag": "ul", "className": "question-list"},
        )
        if root_html is None:
            logger.warning("Root element not found on page=%s", page)
            return []

        soup = BeautifulSoup(root_html, "html.parser")
        post_ids: list[int] = []
        seen: set[int] = set()
        for anchor in soup.find_all("a", class_="e-click-post"):
            href = str(anchor.get("href", ""))
            match = re.search(r"/studies/(\d+)", href)
            if match is None:
                continue
            post_id = int(match.group(1))
            if post_id in seen:
                continue
            seen.add(post_id)
            post_ids.append(post_id)
        logger.info("Parsed post ids from page=%s count=%s", page, len(post_ids))
        return post_ids

    def load_detail(self, post_id: int) -> StudyInfo:
        detail_url_template = str(self.site_config.get("detailUrl", ""))
        if "{}" not in detail_url_template:
            raise ValueError("detailUrl must include '{}' placeholder")

        detail_url = detail_url_template.format(post_id)
        logger.info("Loading detail page: post_id=%s url=%s", post_id, detail_url)
        html = fetch_page_source(detail_url)
        soup = BeautifulSoup(html, "html.parser")
        root = soup.find("div", class_="community-post-info")
        if root is None:
            raise ValueError("community-post-info root not found")

        title_elem = root.select_one(".header__title h1")
        writer_elem = root.select_one("h6.user-name")
        created_elem = root.select_one(".sub-title__created-at .sub-title__value")
        content_elem = root.select_one(".content__body.markdown-body")

        title = title_elem.get_text(strip=True) if title_elem else ""
        writer = writer_elem.get_text(strip=True) if writer_elem else ""
        created_raw = created_elem.get_text(strip=True) if created_elem else ""
        content_raw = content_elem.get_text(" ", strip=True) if content_elem else ""

        if not created_raw:
            raise ValueError("created_at value not found")

        logger.info("Parsed detail success: post_id=%s title=%s", post_id, title)
        return StudyInfo(
            identifier=post_id,
            writer=writer,
            title=title,
            content=self._truncate_content(content_raw, max_length=100),
            url=detail_url,
            created_at=self._to_utc_iso8601(created_raw),
        )

    def apply(self, soup: BeautifulSoup) -> list[StudyInfo]:
        _ = soup
        _, start_page, end_page = self._get_pagination()
        results: list[StudyInfo] = []
        for page in range(start_page, end_page + 1):
            try:
                post_ids = self.load_root_page(page)
            except Exception as exc:
                logger.exception("Failed to load root page %s: %s", page, exc)
                continue

            for post_id in post_ids:
                try:
                    results.append(self.load_detail(post_id))
                except Exception as exc:
                    logger.exception("Failed to load detail for post_id=%s: %s", post_id, exc)
                    continue

        return results

