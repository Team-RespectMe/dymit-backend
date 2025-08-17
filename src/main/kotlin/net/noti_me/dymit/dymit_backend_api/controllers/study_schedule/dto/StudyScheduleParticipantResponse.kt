package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse

@Schema(
    description = "스터디 그룹 일정 참여자 응답",
)
class StudyScheduleParticipantResponse(
    @field:Schema(
        description = "스터디 그룹 일정 ID",
        example = "688c25eb2f3a71dcf291aac9",
    )
    val scheduleId: String,
    @field:Schema(
        description = "참여자 ID",
        example = "688c25eb2f3a71dcf291aac9",
    )
    val memberId: String,
): BaseResponse() {
}