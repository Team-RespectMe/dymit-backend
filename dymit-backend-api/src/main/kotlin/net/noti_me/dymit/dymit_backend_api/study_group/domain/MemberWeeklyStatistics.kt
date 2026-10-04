package net.noti_me.dymit.dymit_backend_api.study_group.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * 가입 구간의 주 종료 시점 누적 통계 스냅샷입니다.
 *
 * @param id 문서 식별자
 * @param groupId 그룹 ID
 * @param membershipId 가입 관계 ID
 * @param memberId 사용자 ID
 * @param weekEnd 한국 시간 월요일 00:00에 해당하는 배타적 상한
 * @param counts 해당 상한까지의 누적 건수
 * @param ledgerVersion 스냅샷을 만든 원장 버전
 * @param projectionId 같은 버전의 동시 투영을 구분하는 식별자
 * @param updatedAt 마지막 저장 시각
 */
@Document("member_weekly_statistics")
@CompoundIndex(
    name = "member_weekly_statistics_membership_week_uq",
    def = "{'membershipId': 1, 'weekEnd': 1}",
    unique = true
)
data class MemberWeeklyStatistics(
    @Id val id: ObjectId? = null,
    @Indexed(name = "member_weekly_statistics_group_idx") val groupId: ObjectId,
    val membershipId: ObjectId,
    val memberId: ObjectId,
    val weekEnd: Instant,
    val counts: StatisticsCounts,
    val ledgerVersion: Long,
    val projectionId: String,
    val updatedAt: Instant
)
