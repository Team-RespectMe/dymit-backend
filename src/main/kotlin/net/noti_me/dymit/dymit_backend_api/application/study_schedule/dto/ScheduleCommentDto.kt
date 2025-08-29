package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.WriterVo
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import java.time.LocalDateTime

class ScheduleCommentDto(
    val id: String,
    val scheduleId: String,
    val writer: WriterVo,
    val createdAt: LocalDateTime,
    val content: String
) {

    companion object {
        fun from(entity: ScheduleComment): ScheduleCommentDto {
            return ScheduleCommentDto(
                id = entity.id.toString(),
                scheduleId = entity.scheduleId.toString(),
                writer = WriterVo.from(entity.writer),
                createdAt = entity.createdAt ?: LocalDateTime.now(),
                content = entity.content
            )
        }
    }
}
