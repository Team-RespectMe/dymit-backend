"""사이트 설정별 전략을 선택해 크롤링을 수행하는 오케스트레이터.

기능:
- 생성자에서 전체 site_config 딕셔너리를 받습니다.
- sites(또는 services) 항목을 순회하며 전략을 생성하고 apply를 실행합니다.

매개변수:
- __init__(site_config): parsing.yaml 로드 결과 전체 딕셔너리
- run(): 모든 사이트에 대해 전략 apply를 수행

반환형:
- run: list[Any] (사이트별 결과 리스트)
"""

from typing import Any
import logging

from bs4 import BeautifulSoup

from app.strategy.strategy_factory import StrategyFactory


logger = logging.getLogger(__name__)


class Crawler:
	def __init__(self, site_config: dict[str, Any]) -> None:
		self.site_config = site_config

	def _resolve_sites(self) -> list[dict[str, Any]]:
		sites = self.site_config.get("sites")
		if sites is None:
			sites = self.site_config.get("services", [])

		if not isinstance(sites, list):
			raise ValueError("sites/services must be a list")

		validated: list[dict[str, Any]] = []
		for site in sites:
			if isinstance(site, dict):
				validated.append(site)
		return validated

	def run(self) -> list[Any]:
		results: list[Any] = []
		for site in self._resolve_sites():
			try:
				strategy = StrategyFactory.create(site)
				results.append(strategy.apply(BeautifulSoup("", "html.parser")))
			except Exception as exc:
				logger.exception("Site crawl failed and will be skipped: %s", exc)
				continue
		return results
