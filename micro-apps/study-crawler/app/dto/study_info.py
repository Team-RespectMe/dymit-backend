"""스터디 모집 정보 DTO.

기능:
- 전략 파서가 추출한 스터디 정보를 공통 형태로 전달합니다.

매개변수:
- identifier (int): 외부 플랫폼 게시물 고유 ID
- writer (str): 작성자 이름
- title (str): 모집글 제목
- content (str): 본문 요약 문자열
- url (str): 상세 페이지 URL
- created_at (str): UTC0 기준 ISO8601 문자열

반환형:
- StudyInfo: 불변 데이터 객체
"""

from dataclasses import dataclass


@dataclass(frozen=True)
class StudyInfo:
    identifier: int
    writer: str
    title: str
    content: str
    url: str
    created_at: str
