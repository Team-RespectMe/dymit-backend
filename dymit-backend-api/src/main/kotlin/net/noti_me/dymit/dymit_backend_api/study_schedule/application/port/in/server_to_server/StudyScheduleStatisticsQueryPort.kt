package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsChanges
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsBoundaryDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsQuery
import org.bson.types.ObjectId
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsReferenceDto

/** 일정 도메인이 제공하는 통계 원천 조회 포트입니다. */
interface StudyScheduleStatisticsQueryPort {
    /** 그룹별 진행 일정 경계를 시간과 ID 순으로 조회합니다. */
    fun loadSessionBoundaries(
        groupIds: List<ObjectId>,
        observedAt: java.time.Instant
    ): List<StudyScheduleStatisticsBoundaryDto>

    /** 삭제되지 않은 일정이 있는 그룹 ID를 조회합니다. */
    fun loadGroupIdsHavingSchedule(groupIds: List<ObjectId>): Set<ObjectId>

    /** 변경되었거나 새로 시작된 일정 원천을 조회합니다. */
    fun loadChanged(query: StudyScheduleStatisticsQuery): StudyScheduleStatisticsChanges

    /** 가입 이후 전체 일정 원천을 조회합니다. */
    fun loadAll(query: StudyScheduleStatisticsQuery): List<StudyScheduleStatisticsDto>

    /** 그룹의 일정 ID를 삭제 여부와 관계없이 일괄 조회합니다. */
    fun loadScheduleReferences(groupId: ObjectId): List<StudyScheduleStatisticsReferenceDto>
}
