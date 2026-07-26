package net.noti_me.dymit.dymit_backend_api.study_schedule.application

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleMemberEventPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleMemberEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleCommentRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.dto.ScheduleCommentWriterUpdateDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class StudyScheduleTransactionHandler(
    private val scheduleCommentRepository: ScheduleCommentRepository
) : StudyScheduleMemberEventPort {

    override fun memberNicknameChanged(event: StudyScheduleMemberEventDto) {
        scheduleCommentRepository.updateWriterInfo(event.toWriterUpdate())
    }

    override fun memberProfileImageChanged(event: StudyScheduleMemberEventDto) {
        scheduleCommentRepository.updateWriterInfo(event.toWriterUpdate())
    }

    private fun StudyScheduleMemberEventDto.toWriterUpdate(): ScheduleCommentWriterUpdateDto {
        return ScheduleCommentWriterUpdateDto(
            memberId = ObjectId(memberId),
            nickname = nickname,
            profileImageType = profileImageType,
            profileImageUrl = profileImageUrl
        )
    }
}
