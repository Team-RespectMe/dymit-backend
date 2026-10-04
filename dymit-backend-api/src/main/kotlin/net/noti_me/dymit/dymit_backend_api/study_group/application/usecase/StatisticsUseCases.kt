package net.noti_me.dymit.dymit_backend_api.study_group.application.usecase

import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.RefreshMemberStatisticsCommand

/** 가입 관계별 통계를 동일한 경로로 갱신하는 유즈케이스입니다. */
fun interface RefreshMemberStatisticsUseCase {
    /** 구성원 원장과 누락된 주간 스냅샷을 갱신합니다. */
    fun execute(command: RefreshMemberStatisticsCommand): MemberStatisticsDto
}

/** 구성원 통계를 조회하는 유즈케이스입니다. */
fun interface GetMemberStatisticsUseCase {
    /** 본인 또는 그룹 관리 권한으로 구성원 통계를 조회합니다. */
    fun execute(command: GetMemberStatisticsCommand): MemberStatisticsDto
}

/** 그룹 통계를 조회하는 유즈케이스입니다. */
fun interface GetGroupStatisticsUseCase {
    /** 그룹 관리 권한으로 전주와 전전주의 누적 비율을 조회합니다. */
    fun execute(command: GetGroupStatisticsCommand): GroupStatisticsDto
}

/** 그룹 구성원 통계 목록을 조회하는 유즈케이스입니다. */
fun interface GetGroupMemberStatisticsUseCase {
    /** 그룹 관리 권한으로 구성원 통계를 커서 페이지 단위로 조회합니다. */
    fun execute(command: GetGroupMemberStatisticsCommand): List<MemberStatisticsDto>
}
