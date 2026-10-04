package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsChanges
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsQuery

/** 과제 도메인이 제공하는 통계 원천 조회 포트입니다. */
interface TaskStatisticsQueryPort {
    /** 변경되었거나 새로 마감된 과제 원천을 조회합니다. */
    fun loadChanged(query: TaskStatisticsQuery): TaskStatisticsChanges

    /** 가입 이후 전체 과제 원천을 조회합니다. */
    fun loadAll(query: TaskStatisticsQuery): List<TaskStatisticsDto>
}
