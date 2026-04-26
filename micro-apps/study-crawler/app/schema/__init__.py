"""schema 패키지 공개 인터페이스.

기능:
- DB 저장용 스키마 객체를 제공합니다.

매개변수:
- 없음

반환형:
- 없음
"""

from app.schema.study_info_schema import StudyInfoSchema

__all__ = ["StudyInfoSchema"]
