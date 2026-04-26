"""Crawler 오케스트레이션 테스트.

기능:
- sites/services 설정 해석과 전략 실행 결과 수집을 검증합니다.
- StrategyFactory를 모킹해 네트워크 없는 단위 테스트를 수행합니다.

매개변수:
- 없음(pytest fixture와 monkeypatch 사용)

반환형:
- 없음(assert 기반 검증)
"""

from typing import Any

import pytest
from bs4 import BeautifulSoup

from app.crawler import Crawler


class _DummyStrategy:
    def __init__(self, payload: Any) -> None:
        self.payload = payload
        self.called_with: list[BeautifulSoup] = []

    def apply(self, soup: BeautifulSoup) -> Any:
        self.called_with.append(soup)
        return self.payload


def test_run_uses_sites_and_collects_results(monkeypatch: Any) -> None:
    site_config = {
        "sites": [
            {"type": "inflearn", "name": "a"},
            {"type": "inflearn", "name": "b"},
        ]
    }

    created: list[_DummyStrategy] = []

    def fake_create(site: dict[str, Any]) -> _DummyStrategy:
        strategy = _DummyStrategy(payload=site["name"])
        created.append(strategy)
        return strategy

    monkeypatch.setattr("app.crawler.StrategyFactory.create", fake_create)

    result = Crawler(site_config).run()

    assert result == ["a", "b"]
    assert len(created) == 2
    assert all(len(strategy.called_with) == 1 for strategy in created)
    assert all(isinstance(strategy.called_with[0], BeautifulSoup) for strategy in created)


def test_run_falls_back_to_services_when_sites_missing(monkeypatch: Any) -> None:
    site_config = {
        "services": [
            {"type": "inflearn", "name": "svc-a"},
        ]
    }

    monkeypatch.setattr(
        "app.crawler.StrategyFactory.create",
        lambda site: _DummyStrategy(payload=site["name"]),
    )

    result = Crawler(site_config).run()

    assert result == ["svc-a"]


def test_run_ignores_non_dict_site_items(monkeypatch: Any) -> None:
    site_config = {
        "sites": [
            {"type": "inflearn", "name": "ok"},
            "skip-me",
            100,
            None,
        ]
    }

    monkeypatch.setattr(
        "app.crawler.StrategyFactory.create",
        lambda site: _DummyStrategy(payload=site["name"]),
    )

    result = Crawler(site_config).run()

    assert result == ["ok"]


def test_resolve_sites_raises_when_sites_is_not_list() -> None:
    crawler = Crawler({"sites": {"type": "inflearn"}})

    with pytest.raises(ValueError, match="sites/services must be a list"):
        crawler.run()
