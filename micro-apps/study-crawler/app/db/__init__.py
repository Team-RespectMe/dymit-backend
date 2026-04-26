"""db 패키지 공개 인터페이스.

기능:
- MongoDB 연결 및 StudyInfo 저장 리포지토리를 제공합니다.

매개변수:
- 없음

반환형:
- 없음
"""

from app.db.mongodb_connection import MongoStudyInfoConnection

__all__ = ["MongoStudyInfoConnection"]
