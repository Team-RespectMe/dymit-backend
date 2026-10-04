package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.statistics

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSourceChanges
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSourceQuery
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskAssignmentData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskExpireAtHistoryData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskStatisticsSourceData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskStatusHistoryData
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.TaskStatisticsQueryPort
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsQuery
import org.springframework.stereotype.Component

/** 과제 도메인 통계 계약을 스터디 그룹 통계 계약으로 변환합니다. */
@Component
class TaskStatisticsSourceAdapter(
    private val queryPort: TaskStatisticsQueryPort
) : TaskStatisticsSourcePort {

    override fun loadChanged(query: StatisticsSourceQuery): StatisticsSourceChanges<TaskStatisticsSourceData> {
        val changes = queryPort.loadChanged(query.toTaskQuery())
        return StatisticsSourceChanges(
            sources = changes.sources.map { it.toSourceData() },
            requiresReplay = changes.requiresReplay
        )
    }

    override fun loadAll(query: StatisticsSourceQuery): List<TaskStatisticsSourceData> {
        return queryPort.loadAll(query.toTaskQuery()).map { it.toSourceData() }
    }

    private fun StatisticsSourceQuery.toTaskQuery() = TaskStatisticsQuery(
        groupId = groupId,
        memberId = memberId,
        joinedAt = joinedAt,
        previousCutoff = previousCutoff,
        cutoff = cutoff,
        mutationAfter = mutationAfter,
        mutationThrough = mutationThrough
    )

    private fun TaskStatisticsDto.toSourceData() = TaskStatisticsSourceData(
        taskId = taskId,
        expireAt = expireAt,
        expireAtHistory = expireAtHistory.map {
            TaskExpireAtHistoryData(it.previousExpireAt, it.expireAt, it.changedAt)
        },
        taskDeletedAt = deletedAt,
        relatedScheduleDeletedAt = relatedScheduleDeletedAt,
        assignments = assignments.map { assignment ->
            TaskAssignmentData(
                assignedAt = assignment.assignedAt,
                deletedAt = assignment.deletedAt,
                statusHistory = assignment.statusHistory.map {
                    TaskStatusHistoryData(it.submitted, it.changedAt)
                },
                legacySubmitted = assignment.legacySubmitted,
                legacyStatusUpdatedAt = assignment.legacyStatusUpdatedAt
            )
        }
    )
}
