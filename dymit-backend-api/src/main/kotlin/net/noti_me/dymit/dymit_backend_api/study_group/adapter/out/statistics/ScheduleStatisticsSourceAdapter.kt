package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.statistics

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleParticipationData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourceData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSourceChanges
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSourceQuery
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleStatisticsQueryPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsQuery
import org.springframework.stereotype.Component

/** 일정 도메인 통계 계약을 스터디 그룹 통계 계약으로 변환합니다. */
@Component
class ScheduleStatisticsSourceAdapter(
    private val queryPort: StudyScheduleStatisticsQueryPort
) : ScheduleStatisticsSourcePort {

    override fun loadChanged(query: StatisticsSourceQuery): StatisticsSourceChanges<ScheduleStatisticsSourceData> {
        val changes = queryPort.loadChanged(query.toScheduleQuery())
        return StatisticsSourceChanges(
            sources = changes.sources.map { it.toSourceData() },
            requiresReplay = changes.requiresReplay
        )
    }

    override fun loadAll(query: StatisticsSourceQuery): List<ScheduleStatisticsSourceData> {
        return queryPort.loadAll(query.toScheduleQuery()).map { it.toSourceData() }
    }

    private fun StatisticsSourceQuery.toScheduleQuery() = StudyScheduleStatisticsQuery(
        groupId = groupId,
        memberId = memberId,
        joinedAt = joinedAt,
        previousCutoff = previousCutoff,
        cutoff = cutoff,
        mutationAfter = mutationAfter,
        mutationThrough = mutationThrough
    )

    private fun StudyScheduleStatisticsDto.toSourceData() = ScheduleStatisticsSourceData(
        scheduleId = scheduleId,
        createdAt = createdAt,
        scheduleAt = scheduleAt,
        scheduleDeletedAt = deletedAt,
        participations = participations.map {
            ScheduleParticipationData(it.participatedAt, it.deletedAt)
        }
    )
}
