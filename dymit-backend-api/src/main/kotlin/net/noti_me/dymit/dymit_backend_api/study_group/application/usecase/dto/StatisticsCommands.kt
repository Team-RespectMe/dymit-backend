package net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto

import java.time.Instant

/** 구성원 통계 원장 갱신 명령입니다. */
data class RefreshMemberStatisticsCommand(
    val groupId: String,
    val membershipId: String,
    val cutoff: Instant,
    val observedAt: Instant
)

/** 구성원 통계 조회 명령입니다. */
data class GetMemberStatisticsCommand(
    val requesterId: String,
    val groupId: String,
    val memberId: String
)

/** 그룹 통계 조회 명령입니다. */
data class GetGroupStatisticsCommand(
    val requesterId: String,
    val groupId: String
)

/** 그룹 구성원 통계 목록 조회 명령입니다. */
data class GetGroupMemberStatisticsCommand(
    val requesterId: String,
    val groupId: String,
    val cursor: String? = null,
    val size: Int = 20
)
