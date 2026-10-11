package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.ManagedTaskDto
import java.time.Instant

/** 소유 그룹의 오늘 마감 과제 목록 항목 응답입니다. */
class ManagedTaskResponse(
    val group: ManagedTaskGroupResponse,
    val taskId: String,
    val title: String,
    val expireAt: Instant,
    val notSubmittedAssigneeCount: Long
) : BaseResponse() {

    companion object {

        /** 애플리케이션 DTO를 공개 응답으로 변환합니다. */
        fun from(dto: ManagedTaskDto): ManagedTaskResponse {
            return ManagedTaskResponse(
                group = ManagedTaskGroupResponse(dto.group.id, dto.group.name),
                taskId = dto.taskId,
                title = dto.title,
                expireAt = dto.expireAt,
                notSubmittedAssigneeCount = dto.notSubmittedAssigneeCount
            )
        }
    }
}

/** 관리 과제 응답에 포함되는 그룹 요약입니다. */
class ManagedTaskGroupResponse(
    val id: String,
    val name: String
)
