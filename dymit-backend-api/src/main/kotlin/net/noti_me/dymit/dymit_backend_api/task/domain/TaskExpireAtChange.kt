package net.noti_me.dymit.dymit_backend_api.task.domain

import java.time.Instant

/**
 * 과제 마감 시각 변경 이력입니다.
 *
 * @param previousExpireAt 변경 전 마감 시각
 * @param expireAt 변경 후 마감 시각
 * @param changedAt 변경 시각
 */
data class TaskExpireAtChange(
    val previousExpireAt: Instant,
    val expireAt: Instant,
    val changedAt: Instant
)
