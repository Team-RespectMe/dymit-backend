package net.noti_me.dymit.dymit_backend_api.study_group.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * 가입 구간별 최신 누적 통계와 처리 커서를 보관하는 원장입니다.
 *
 * @param id 문서 식별자
 * @param groupId 그룹 ID
 * @param membershipId 가입 관계 ID
 * @param memberId 사용자 ID
 * @param counts 누적 통계 건수
 * @param taskCalculatedThrough 과제 업무 계산 상한
 * @param scheduleCalculatedThrough 일정 업무 계산 상한
 * @param taskMutationWatermark 과제 변경 검색 성공 시각
 * @param scheduleMutationWatermark 일정 변경 검색 성공 시각
 * @param version 원장 버전
 * @param projectionId 같은 버전의 동시 투영을 구분하는 식별자
 * @param projectionStartWeekEnd 현재 버전이 교체해야 하는 첫 주 종료 시각
 * @param updatedAt 마지막 저장 시각
 */
@Document("member_statistics_ledgers")
@CompoundIndex(name = "member_statistics_membership_uq", def = "{'membershipId': 1}", unique = true)
data class MemberStatisticsLedger(
    @Id val id: ObjectId? = null,
    val groupId: ObjectId,
    val membershipId: ObjectId,
    val memberId: ObjectId,
    val counts: StatisticsCounts,
    val taskCalculatedThrough: Instant,
    val scheduleCalculatedThrough: Instant,
    val taskMutationWatermark: Instant,
    val scheduleMutationWatermark: Instant,
    val version: Long,
    val projectionId: String,
    val projectionStartWeekEnd: Instant,
    val updatedAt: Instant
)
