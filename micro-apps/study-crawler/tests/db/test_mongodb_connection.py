"""MongoStudyInfoConnection 테스트.

기능:
- 환경 변수 로드와 StudyInfo 저장 요청 시 문서 매핑을 검증합니다.

매개변수:
- 없음(pytest fixture와 monkeypatch 사용)

반환형:
- 없음(assert 기반 검증)
"""

from typing import Any

from app.dto.study_info import StudyInfo
from app.db.mongodb_connection import MongoStudyInfoConnection


class _FakeUpdateOneResult:
    def __init__(self, upserted_id: Any) -> None:
        self.upserted_id = upserted_id


class _FakeBulkWriteResult:
    def __init__(self, upserted_ids: dict[int, Any]) -> None:
        self.upserted_ids = upserted_ids


class _FakeCollection:
    def __init__(self) -> None:
        self.saved_one_filter: dict[str, Any] | None = None
        self.saved_one_update: dict[str, Any] | None = None
        self.saved_one_upsert: bool = False
        self.saved_many_ops: list[Any] = []

    def update_one(
        self,
        query: dict[str, Any],
        update: dict[str, Any],
        upsert: bool,
    ) -> _FakeUpdateOneResult:
        self.saved_one_filter = query
        self.saved_one_update = update
        self.saved_one_upsert = upsert
        return _FakeUpdateOneResult("id-1")

    def bulk_write(self, operations: list[Any], ordered: bool) -> _FakeBulkWriteResult:
        _ = ordered
        self.saved_many_ops = operations
        return _FakeBulkWriteResult({0: "id-1", 1: "id-2"})


class _FakeDatabase:
    def __init__(self, collection: _FakeCollection) -> None:
        self.collection = collection

    def __getitem__(self, _: str) -> _FakeCollection:
        return self.collection


class _FakeMongoClient:
    def __init__(self, *args: Any, **kwargs: Any) -> None:
        self.args = args
        self.kwargs = kwargs
        self.collection = _FakeCollection()
        self.database = _FakeDatabase(self.collection)

    def __getitem__(self, _: str) -> _FakeDatabase:
        return self.database


def _sample_study(identifier: int = 1) -> StudyInfo:
    return StudyInfo(
        identifier=identifier,
        writer="writer",
        title="title",
        content="content",
        url=f"https://example.com/studies/{identifier}",
        created_at="2026-04-25T13:41:00Z",
    )


def test_init_uses_default_env_values_when_missing(monkeypatch: Any) -> None:
    monkeypatch.delenv("MONGO_HOST", raising=False)
    monkeypatch.delenv("MONGO_PORT", raising=False)
    monkeypatch.delenv("MONGO_DB_NAME", raising=False)
    monkeypatch.delenv("MONGO_INITDB_ROOT_USERNAME", raising=False)
    monkeypatch.delenv("MONGO_INITDB_ROOT_PASSWORD", raising=False)
    monkeypatch.delenv("DYMIT_STUDY_CRAWLER_COLLECTION_NAME", raising=False)
    monkeypatch.delenv("DYMIT_MONGODB_AUTHENTICATION_DATABASE", raising=False)
    monkeypatch.setattr("app.db.mongodb_connection.MongoClient", _FakeMongoClient)

    connection = MongoStudyInfoConnection()

    assert connection.db_host == "localhost"
    assert connection.db_port == "27017"
    assert connection.db_name == "dymit"
    assert connection.db_user == "test_admin"
    assert connection.db_password == "test1234"
    assert connection.collection_name == "study_recruitments"


def test_insert_one_maps_schema_and_returns_inserted_id(monkeypatch: Any) -> None:
    monkeypatch.setenv("MONGO_DB_NAME", "dymit")
    monkeypatch.setenv("MONGO_INITDB_ROOT_USERNAME", "test_admin")
    monkeypatch.setenv("MONGO_INITDB_ROOT_PASSWORD", "pw")
    monkeypatch.setenv("DYMIT_STUDY_CRAWLER_COLLECTION_NAME", "study_infos")
    monkeypatch.setattr("app.db.mongodb_connection.MongoClient", _FakeMongoClient)

    connection = MongoStudyInfoConnection()
    inserted_id = connection.insert_one(_sample_study(77), source_type="INFLEARN")

    assert inserted_id == "id-1"
    assert connection.collection.saved_one_filter == {"type": "INFLEARN", "externalId": 77}
    assert connection.collection.saved_one_update is not None
    assert connection.collection.saved_one_update["$set"]["externalId"] == 77
    assert connection.collection.saved_one_update["$set"]["type"] == "INFLEARN"
    assert connection.collection.saved_one_upsert is True


def test_insert_many_returns_empty_for_empty_input(monkeypatch: Any) -> None:
    monkeypatch.setenv("MONGO_DB_NAME", "dymit")
    monkeypatch.setenv("MONGO_INITDB_ROOT_USERNAME", "test_admin")
    monkeypatch.setenv("MONGO_INITDB_ROOT_PASSWORD", "pw")
    monkeypatch.setenv("DYMIT_STUDY_CRAWLER_COLLECTION_NAME", "study_infos")
    monkeypatch.setattr("app.db.mongodb_connection.MongoClient", _FakeMongoClient)

    connection = MongoStudyInfoConnection()
    inserted_ids = connection.insert_many([])

    assert inserted_ids == []


def test_insert_many_uses_bulk_upsert(monkeypatch: Any) -> None:
    monkeypatch.setenv("MONGO_DB_NAME", "dymit")
    monkeypatch.setenv("MONGO_INITDB_ROOT_USERNAME", "test_admin")
    monkeypatch.setenv("MONGO_INITDB_ROOT_PASSWORD", "pw")
    monkeypatch.setenv("DYMIT_STUDY_CRAWLER_COLLECTION_NAME", "study_infos")
    monkeypatch.setattr("app.db.mongodb_connection.MongoClient", _FakeMongoClient)

    connection = MongoStudyInfoConnection()
    inserted_ids = connection.insert_many([_sample_study(1), _sample_study(2)])

    assert inserted_ids == ["id-1", "id-2"]
    assert len(connection.collection.saved_many_ops) == 2
