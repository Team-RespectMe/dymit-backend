"""MongoDB 연결 및 StudyInfo 저장 기능.

기능:
- 시스템 환경 변수로 MongoDB 연결 대상을 구성합니다.
- StudyInfo DTO를 StudyInfoSchema로 변환해 컬렉션에 저장합니다.

매개변수:
- __init__(): 환경 변수 로드 및 MongoClient 초기화
- insert_one(study_info, source_type): 단건 upsert 저장
- insert_many(study_infos, source_type): 다건 upsert 저장

반환형:
- insert_one: ObjectId | None
- insert_many: list[ObjectId]
"""

import os
from typing import Iterable

from bson import ObjectId
from pymongo import MongoClient, UpdateOne
from pymongo.collection import Collection
from pymongo.database import Database

from app.dto.study_info import StudyInfo
from app.schema.study_info_schema import StudyInfoSchema


class MongoStudyInfoConnection:
    def __init__(self) -> None:
        self.db_host = os.getenv("MONGO_HOST", "localhost")
        self.db_port = os.getenv("MONGO_PORT", "27017")
        self.db_name = os.getenv("MONGO_DB_NAME", "dymit")
        self.db_user = os.getenv("MONGO_INITDB_ROOT_USERNAME", "test_admin")
        self.db_password = os.getenv("MONGO_INITDB_ROOT_PASSWORD", "test1234")
        self.collection_name = os.getenv("DYMIT_STUDY_CRAWLER_COLLECTION_NAME", "study_recruitments")
        self.auth_source = os.getenv("DYMIT_MONGODB_AUTHENTICATION_DATABASE", "dymit")

        self.client = MongoClient(
            host=self.db_host,
            port=int(self.db_port),
            username=self.db_user,
            password=self.db_password,
            authSource=self.auth_source,
        )
        self.database: Database = self.client[self.db_name]
        self.collection: Collection = self.database[self.collection_name]

    @staticmethod
    def _require_env(name: str) -> str:
        value = os.getenv(name)
        if not value:
            raise ValueError(f"Environment variable '{name}' is required")
        return value

    def insert_one(self, study_info: StudyInfo, source_type: str = "INFLEARN") -> ObjectId | None:
        schema = StudyInfoSchema.from_dto(study_info, source_type=source_type)
        document = schema.to_document()
        result = self.collection.update_one(
            {"type": document["type"], "externalId": document["externalId"]},
            {"$set": document},
            upsert=True,
        )
        return result.upserted_id

    def insert_many(
        self,
        study_infos: Iterable[StudyInfo],
        source_type: str = "INFLEARN",
    ) -> list[ObjectId]:
        operations = [
            UpdateOne(
                {
                    "type": schema_doc["type"],
                    "externalId": schema_doc["externalId"],
                },
                {"$set": schema_doc},
                upsert=True,
            )
            for schema_doc in [
                StudyInfoSchema.from_dto(study_info, source_type=source_type).to_document()
                for study_info in study_infos
            ]
        ]
        if not operations:
            return []
        result = self.collection.bulk_write(operations, ordered=False)
        return [inserted_id for _, inserted_id in sorted(result.upserted_ids.items())]
