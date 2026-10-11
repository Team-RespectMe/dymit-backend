package net.noti_me.dymit.dymit_backend_api.task.adapter.out.persistence.mongo

import jakarta.annotation.PostConstruct
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.stereotype.Component

/** 관리 과제 목록의 날짜 커서 조회와 미제출 집계 인덱스를 보장합니다. */
@Component
class ManagedTaskIndexInitializer(
    private val mongoTemplate: MongoTemplate
) {

    /** 애플리케이션 시작 시 관리 과제 조회 인덱스를 생성합니다. */
    @PostConstruct
    fun ensureIndexes() {
        mongoTemplate.indexOps("tasks").ensureIndex(
            CompoundIndexDefinition(
                Document(mapOf("expireAt" to 1, "_id" to 1, "isDeleted" to 1))
            ).named("managed_task_expire_id_active_idx")
        )
        mongoTemplate.indexOps("task_assignees").ensureIndex(
            CompoundIndexDefinition(
                Document(mapOf("taskId" to 1, "status" to 1, "isDeleted" to 1))
            ).named("managed_task_assignee_status_active_idx")
        )
    }
}
