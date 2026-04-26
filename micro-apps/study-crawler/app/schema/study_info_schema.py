"""StudyInfo DTO를 MongoDB 저장 문서로 변환하는 스키마.

기능:
- StudyInfo를 Camel Case 필드의 DB 문서 형태로 변환합니다.
- _id는 None일 때 문서에 포함하지 않아 MongoDB 자동 생성에 맡깁니다.

매개변수:
- from_dto(study_info, source_type): DTO와 소스 타입을 받아 스키마 생성
- to_document(): MongoDB 삽입용 dict 반환

반환형:
- StudyInfoSchema / dict[str, Any]
"""

from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any

from bson import ObjectId

from app.dto.study_info import StudyInfo


@dataclass(frozen=True)
class StudyInfoSchema:
    _id: ObjectId | None
    writer: str
    type: str
    external_id: int
    url: str
    title: str
    content: str
    created_at: datetime
    updated_at: datetime
    is_deleted: bool = False
    _class: str = "net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment"

    @staticmethod
    def _parse_iso_datetime(value: str) -> datetime:
        normalized = value.replace("Z", "+00:00")
        parsed = datetime.fromisoformat(normalized)
        if parsed.tzinfo is None:
            parsed = parsed.replace(tzinfo=timezone.utc)
        return parsed.astimezone(timezone.utc)

    @classmethod
    def from_dto(cls, study_info: StudyInfo, source_type: str = "INFLEARN") -> "StudyInfoSchema":
        created_at = cls._parse_iso_datetime(study_info.created_at)
        return cls(
            _id=None,
            writer=study_info.writer,
            type=source_type,
            external_id=study_info.identifier,
            url=study_info.url,
            title=study_info.title,
            content=study_info.content,
            created_at=created_at,
            updated_at=created_at,
            is_deleted=False,
        )

    def to_document(self) -> dict[str, Any]:
        document: dict[str, Any] = {
            "writer": self.writer,
            "type": self.type,
            "externalId": self.external_id,
            "url": self.url,
            "title": self.title,
            "content": self.content,
            "createdAt": self.created_at,
            "updatedAt": self.updated_at,
            "isDeleted": self.is_deleted,
            "_class": self._class,
        }
        if self._id is not None:
            document["_id"] = self._id
        return document
