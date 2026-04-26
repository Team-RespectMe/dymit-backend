"""StudyInfoSchema 테스트.

기능:
- StudyInfo DTO -> DB 문서 변환 결과를 검증합니다.

매개변수:
- 없음(pytest 기반)

반환형:
- 없음(assert 기반 검증)
"""

from datetime import datetime, timezone

from app.dto.study_info import StudyInfo
from app.schema.study_info_schema import StudyInfoSchema


def test_study_info_schema_to_document_uses_camel_case_keys() -> None:
    dto = StudyInfo(
        identifier=99,
        writer="writer",
        title="title",
        content="content",
        url="https://example.com/studies/99",
        created_at="2026-04-25T13:41:00Z",
    )

    schema = StudyInfoSchema.from_dto(dto, source_type="INFLEARN")
    document = schema.to_document()

    assert "_id" not in document
    assert document["writer"] == "writer"
    assert document["type"] == "INFLEARN"
    assert document["externalId"] == 99
    assert document["url"] == "https://example.com/studies/99"
    assert document["title"] == "title"
    assert document["content"] == "content"
    assert document["createdAt"] == datetime(2026, 4, 25, 13, 41, 0, tzinfo=timezone.utc)
    assert document["updatedAt"] == datetime(2026, 4, 25, 13, 41, 0, tzinfo=timezone.utc)
    assert document["isDeleted"] is False
    assert (
        document["_class"]
        == "net.noti_me.dymit.dymit_backend_api.domain.study.Recruitment"
    )
