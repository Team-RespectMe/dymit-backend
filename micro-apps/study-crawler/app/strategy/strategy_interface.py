"""파싱 전략 인터페이스(추상 클래스) 정의.

기능:
- 각 사이트별 파서 전략이 공통으로 구현해야 할 생성자와 apply 메서드를 정의합니다.
- 생성자에서 site_config(사이트 설정 1개)를 전달받아 저장합니다.

매개변수:
- __init__(site_config): SiteConfigLoader 반환 딕셔너리의 sites 항목 중 하나
- apply(soup): 파싱 대상 BeautifulSoup 객체

반환형:
- apply: 구현체가 정의하는 파싱 결과(Any)
"""

from abc import ABC, abstractmethod
from typing import Any

from bs4 import BeautifulSoup


class Strategy(ABC):
    def __init__(self, site_config: dict[str, Any]) -> None:
        self.site_config: dict[str, Any] = site_config

    @abstractmethod
    def apply(self, soup: BeautifulSoup) -> Any:
        """BeautifulSoup 입력을 받아 사이트별 파싱 결과를 반환합니다."""
