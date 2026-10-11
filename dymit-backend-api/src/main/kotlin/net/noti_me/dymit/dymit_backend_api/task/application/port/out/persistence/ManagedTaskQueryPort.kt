package net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.task.application.ManagedTaskCursor
import org.bson.types.ObjectId
import java.time.Instant

/** 소유 그룹의 마감 과제 목록을 읽기 전용으로 조회하는 포트입니다. */
interface ManagedTaskQueryPort {

    /** 소유자와 마감일, 복합 커서에 맞는 과제를 정렬하여 조회합니다. */
    fun load(
        ownerId: ObjectId,
        startInclusive: Instant,
        endExclusive: Instant,
        cursor: ManagedTaskCursor?,
        limit: Int
    ): List<ManagedTaskQueryResult>

    /** 과제별 유효한 미제출 대상자 수를 일괄 조회합니다. */
    fun countNotSubmittedAssignees(taskIds: List<ObjectId>): Map<ObjectId, Long>
}

/** 관리 과제 Mongo 조회 결과입니다. */
data class ManagedTaskQueryResult(
    val taskId: ObjectId,
    val title: String,
    val expireAt: Instant,
    val groupId: ObjectId,
    val groupName: String
)
