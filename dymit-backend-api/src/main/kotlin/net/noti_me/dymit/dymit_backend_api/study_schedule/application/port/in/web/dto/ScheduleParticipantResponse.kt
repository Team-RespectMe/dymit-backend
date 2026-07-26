package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleParticipantDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse

class ScheduleParticipantResponse(
    val scheduleId: String,
    val memberId: String,
    val nickname: String,
    val image: StudyScheduleProfileImageResponse
): BaseResponse() {

    companion object {
        fun from(dto: StudyScheduleParticipantDto): ScheduleParticipantResponse {
            return ScheduleParticipantResponse(
                scheduleId = dto.scheduleId,
                memberId = dto.memberId,
                nickname = dto.nickname,
                image = StudyScheduleProfileImageResponse.of(dto.image.type, dto.image.url)
            )
        }
    }
}
