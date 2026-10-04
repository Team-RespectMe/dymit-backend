package net.noti_me.dymit.dymit_backend_api.study_group.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * 그룹의 주 종료 시점 누적 통계 스냅샷입니다.
 *
 * @param id 문서 식별자
 * @param groupId 그룹 ID
 * @param weekEnd 한국 시간 월요일 00:00에 해당하는 배타적 상한
 * @param counts 구성원 스냅샷 건수의 합
 * @param version 그룹 스냅샷 버전
 * @param updatedAt 마지막 저장 시각
 */
@Document("group_weekly_statistics")
@CompoundIndex(
    name = "group_weekly_statistics_group_week_uq",
    def = "{'groupId': 1, 'weekEnd': 1}",
    unique = true
)
data class GroupWeeklyStatistics(
    @Id val id: ObjectId? = null,
    val groupId: ObjectId,
    val weekEnd: Instant,
    val counts: StatisticsCounts,
    val version: Long,
    val updatedAt: Instant
)
