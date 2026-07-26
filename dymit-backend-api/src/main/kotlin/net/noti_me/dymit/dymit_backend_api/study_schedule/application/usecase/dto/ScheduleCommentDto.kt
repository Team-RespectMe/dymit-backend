package net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleComment
import java.time.LocalDateTime

class ScheduleCommentDto(
    val id: String,
    val scheduleId: String,
    val writer: ScheduleCommentWriterDto,
    val createdAt: LocalDateTime,
    val content: String
) {

    companion object {
        fun from(entity: ScheduleComment): ScheduleCommentDto {
            return ScheduleCommentDto(
                id = entity.id.toString(),
                scheduleId = entity.scheduleId.toString(),
                writer = ScheduleCommentWriterDto(
                    memberId = entity.writer.id.toHexString(),
                    nickname = entity.writer.nickname,
                    image = entity.writer.image
                ),
                createdAt = entity.createdAt ?: LocalDateTime.now(),
                content = entity.content
            )
        }
    }
}

data class ScheduleCommentWriterDto(
    val memberId: String,
    val nickname: String,
    val image: net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupProfileImageDto
)
