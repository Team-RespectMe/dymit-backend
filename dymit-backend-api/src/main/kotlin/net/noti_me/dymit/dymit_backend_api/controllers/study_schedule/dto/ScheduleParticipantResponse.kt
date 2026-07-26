package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleParticipantDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.controllers.common.ProfileImageResponse
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupProfileImageDto as ProfileImageVo

class ScheduleParticipantResponse(
    val scheduleId: String,
    val memberId: String,
    val nickname: String,
    val image: ProfileImageResponse
): BaseResponse() {

    companion object {
        fun from(dto: StudyScheduleParticipantDto): ScheduleParticipantResponse {
            return ScheduleParticipantResponse(
                scheduleId = dto.scheduleId,
                memberId = dto.memberId,
                nickname = dto.nickname,
                image = ProfileImageResponse.of(dto.image.type, dto.image.url)
            )
        }
    }
}
