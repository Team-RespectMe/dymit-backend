"""strategy 패키지 공개 인터페이스.

기능:
- 파싱 전략 추상 인터페이스를 외부에서 import 할 수 있게 제공합니다.

매개변수:
- 없음

반환형:
- 없음
"""

from app.strategy.strategy_interface import Strategy

__all__ = ["Strategy"]
