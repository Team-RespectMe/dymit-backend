"""사이트 타입별 전략 인스턴스를 생성하는 팩토리.

기능:
- site_config의 type 값에 맞는 전략 클래스를 선택해 생성합니다.
- 현재 inflearn 타입을 지원합니다.

매개변수:
- create(site): sites/services 항목 중 하나의 설정 딕셔너리

반환형:
- Strategy: 선택된 전략 인스턴스
"""

from importlib import util
from pathlib import Path
from typing import Any

from app.strategy.strategy_interface import Strategy


class StrategyFactory:
    _inflearn_class: type[Strategy] | None = None

    @classmethod
    def _load_inflearn_class(cls) -> type[Strategy]:
        if cls._inflearn_class is not None:
            return cls._inflearn_class

        module_path = Path(__file__).resolve().parent / "inflearn-strategy.py"
        spec = util.spec_from_file_location("inflearn_strategy", module_path)
        if spec is None or spec.loader is None:
            raise RuntimeError("Cannot load inflearn strategy module")

        module = util.module_from_spec(spec)
        spec.loader.exec_module(module)
        inflearn_class = getattr(module, "InflearnStrategy", None)
        if inflearn_class is None:
            raise RuntimeError("InflearnStrategy class not found")

        cls._inflearn_class = inflearn_class
        return cls._inflearn_class

    @classmethod
    def create(cls, site: dict[str, Any]) -> Strategy:
        site_type = str(site.get("type", "")).strip().lower()
        if site_type == "inflearn":
            return cls._load_inflearn_class()(site)

        raise ValueError(f"Unsupported strategy type: {site_type}")
