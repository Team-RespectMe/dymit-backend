"""HTML 문자열(또는 파싱 구조)에서 루트 태그를 기준으로 데이터를 추출하는 파서.

기능:
- 루트 태그(tag, className)에 해당하는 전체 엘리먼트 HTML을 반환합니다.
- 루트 태그(tag, className)에 해당하는 내부 텍스트를 단일 문자열로 반환합니다.

매개변수:
- html_data (str | BeautifulSoup | Tag): 원본 HTML 문자열 또는 이미 파싱된 구조
- root (RootTag): 탐색 기준 루트 태그 정보(tag, className)

반환형:
- extract_root_element_html: str | None
- extract_root_text: str | None
"""

from typing import TypedDict

from bs4 import BeautifulSoup, Tag


class RootTag(TypedDict):
    tag: str
    className: str


def _to_soup(html_data: str | BeautifulSoup | Tag) -> BeautifulSoup:
    if isinstance(html_data, BeautifulSoup):
        return html_data

    if isinstance(html_data, Tag):
        return BeautifulSoup(str(html_data), "html.parser")

    return BeautifulSoup(html_data, "html.parser")


def extract_root_element_html(html_data: str | BeautifulSoup | Tag, root: RootTag) -> str | None:
    soup = _to_soup(html_data)
    element = soup.find(root["tag"], class_=root["className"])
    if element is None:
        return None
    return str(element)


def extract_root_text(html_data: str | BeautifulSoup | Tag, root: RootTag) -> str | None:
    soup = _to_soup(html_data)
    element = soup.find(root["tag"], class_=root["className"])
    if element is None:
        return None
    return element.get_text(strip=True)
