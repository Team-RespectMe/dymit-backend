"""InflearnStrategy 동작 테스트.

기능:
- 목록 페이지 순회, postId 추출, 상세 파싱, UTC 변환 로직을 검증합니다.

매개변수:
- 없음(pytest fixture와 monkeypatch 사용)

반환형:
- 없음(assert 기반 검증)
"""

from importlib import util
from pathlib import Path
from typing import Any

from bs4 import BeautifulSoup


MODULE_PATH = (
    Path(__file__).resolve().parent.parent.parent / "app/strategy/inflearn-strategy.py"
)
SPEC = util.spec_from_file_location("inflearn_strategy", MODULE_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError("Cannot load inflearn-strategy.py module")
MODULE = util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)
InflearnStrategy = MODULE.InflearnStrategy


def test_load_root_page_extracts_post_ids(monkeypatch: Any) -> None:
    site_config = {
        "listUrl": "https://www.inflearn.com/community/studies?page={}&order=recent",
        "pagination": [{"pageParam": "page", "startPage": 1, "endPage": 1}],
        "detailUrl": "https://www.inflearn.com/studies/{}",
    }
    strategy = InflearnStrategy(site_config)

    list_html = """
    <ul class="question-list">
      <li><a class="e-click-post" href="/studies/101/alpha">alpha</a></li>
      <li><a class="e-click-post" href="/studies/202/beta">beta</a></li>
      <li><a class="e-click-post" href="/studies/101/alpha">dup</a></li>
    </ul>
    """

    monkeypatch.setattr(MODULE, "fetch_page_source", lambda _: list_html)

    result = strategy.load_root_page(1)

    assert result == [101, 202]


def test_load_detail_parses_fields_and_converts_utc(monkeypatch: Any) -> None:
    site_config = {
        "listUrl": "https://www.inflearn.com/community/studies?page={}&order=recent",
        "pagination": [{"pageParam": "page", "startPage": 1, "endPage": 1}],
        "detailUrl": "https://www.inflearn.com/studies/{}",
    }
    strategy = InflearnStrategy(site_config)

    detail_html = """
    <div class='community-post-info'>
      <div class='community-post-info__header'>
        <div class='header__title'><h1>같이 카공할 분 구합니당</h1></div>
        <div class='header__sub-title'>
          <div class='header__sub-title__content'>
            <h6 class='user-name'><a href='/users/1/@x'>해리</a></h6>
            <div class='content-date'>
              <span class='sub-title sub-title__created-at'>
                <span class='sub-title__title'>작성일</span>
                <span class='sub-title__value'>26.04.25 22:41</span>
              </span>
            </div>
          </div>
        </div>
      </div>
      <div class='content__body markdown-body'>
        여기는 테스트 본문입니다. 여기는 테스트 본문입니다. 여기는 테스트 본문입니다. 여기는 테스트 본문입니다.
      </div>
    </div>
    """

    monkeypatch.setattr(MODULE, "fetch_page_source", lambda _: detail_html)

    result = strategy.load_detail(123)

    assert result.title == "같이 카공할 분 구합니당"
    assert result.writer == "해리"
    assert result.identifier == 123
    assert result.url == "https://www.inflearn.com/studies/123"
    assert result.created_at == "2026-04-25T13:41:00Z"
    assert len(result.content) <= 100


def test_apply_runs_page_cycle_and_collects_details(monkeypatch: Any) -> None:
    site_config = {
        "listUrl": "https://www.inflearn.com/community/studies?page={}&order=recent",
        "pagination": [{"pageParam": "page", "startPage": 1, "endPage": 2}],
        "detailUrl": "https://www.inflearn.com/studies/{}",
    }
    strategy = InflearnStrategy(site_config)

    list_page_1 = """
    <ul class='question-list'>
      <li><a class='e-click-post' href='/studies/10/a'>a</a></li>
    </ul>
    """
    list_page_2 = """
    <ul class='question-list'>
      <li><a class='e-click-post' href='/studies/20/b'>b</a></li>
    </ul>
    """
    detail_template = """
    <div class='community-post-info'>
      <div class='header__title'><h1>title-{pid}</h1></div>
      <h6 class='user-name'><a href='/users/1/@x'>writer-{pid}</a></h6>
      <span class='sub-title sub-title__created-at'>
        <span class='sub-title__value'>26.04.25 22:41</span>
      </span>
      <div class='content__body markdown-body'>content-{pid}</div>
    </div>
    """

    def fake_fetch(url: str) -> str:
        if "community/studies" in url and "page=1" in url:
            return list_page_1
        if "community/studies" in url and "page=2" in url:
            return list_page_2
        if "/studies/10" in url:
            return detail_template.format(pid=10)
        if "/studies/20" in url:
            return detail_template.format(pid=20)
        return ""

    monkeypatch.setattr(MODULE, "fetch_page_source", fake_fetch)

    result = strategy.apply(BeautifulSoup("<div></div>", "html.parser"))

    assert [item.identifier for item in result] == [10, 20]
    assert [item.title for item in result] == ["title-10", "title-20"]
    assert [item.writer for item in result] == ["writer-10", "writer-20"]

