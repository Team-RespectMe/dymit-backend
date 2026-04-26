"""http_request 모듈 테스트.

기능:
- 페이지 소스 반환 함수를 외부 네트워크 없이 검증합니다.

매개변수:
- 없음(pytest fixture와 monkeypatch 사용)

반환형:
- 없음(assert 기반 검증)
"""

from typing import Any

import app.simple_http.http_request as http_request


class _DummyResponse:
    def __init__(self, text: str) -> None:
        self.text = text

    def raise_for_status(self) -> None:
        return None


def test_fetch_page_source_returns_response_text(monkeypatch: Any) -> None:
    def fake_get(addr: str, timeout: int) -> _DummyResponse:
        assert addr == "https://example.com"
        assert timeout == 10
        return _DummyResponse("<html>ok</html>")

    monkeypatch.setattr(http_request.requests, "get", fake_get)

    result = http_request.fetch_page_source("https://example.com")

    assert result == "<html>ok</html>"
