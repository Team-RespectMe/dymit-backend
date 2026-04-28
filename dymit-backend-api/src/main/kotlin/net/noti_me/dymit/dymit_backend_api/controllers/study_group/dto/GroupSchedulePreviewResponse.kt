package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import java.time.LocalDateTime

@Schema(
    description = "스터디 그룹 일정 미리보기 응답 DTO",
)
class GroupSchedulePreviewResponse(
    @Schema(description = "스터디 그룹 일정 식별자")
    val id: String,
    @Schema(description = "스터디 그룹 일정 제목")
    val title: String,
    @Schema(description = "스터디 예징 일시")
    val scheduleAt: LocalDateTime,
    @Schema(description = "스터디 예정 장소")
    val location: String = "",
): BaseResponse() {
}