package net.noti_me.dymit.dymit_backend_api.task.domain

import java.time.Instant

/**
 * 과제 대상자의 제출 상태 변경 이력입니다.
 *
 * @param status 변경된 제출 상태
 * @param changedAt 상태가 변경된 시각
 */
data class TaskAssigneeStatusChange(
    val status: TaskAssigneeStatus,
    val changedAt: Instant
)
