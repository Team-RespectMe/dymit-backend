"""http_parser 모듈 테스트.

기능:
- 루트 엘리먼트 HTML 추출 함수와 루트 내부 텍스트 추출 함수를 검증합니다.

매개변수:
- 없음(pytest가 테스트 함수를 직접 실행)

반환형:
- 없음(assert 기반 검증)
"""

from app.simple_http.http_parser import extract_root_element_html, extract_root_text


def test_extract_root_element_html_returns_wrapped_html() -> None:
    html = '<div class="root"><span>hello</span></div>'
    root = {"tag": "div", "className": "root"}

    result = extract_root_element_html(html, root)

    assert result == '<div class="root"><span>hello</span></div>'


def test_extract_root_text_returns_inner_text() -> None:
    html = '<div class="root"><span>hello</span></div>'
    root = {"tag": "div", "className": "root"}

    result = extract_root_text(html, root)

    assert result == "hello"


def test_extract_root_text_returns_none_when_not_found() -> None:
    html = '<div class="other">hello</div>'
    root = {"tag": "div", "className": "root"}

    result = extract_root_text(html, root)

    assert result is None
