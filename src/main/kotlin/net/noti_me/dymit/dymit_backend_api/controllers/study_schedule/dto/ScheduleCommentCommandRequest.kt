package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "일정 댓글 생성 요청")
class ScheduleCommentCommandRequest(
    @Schema(description = "댓글 내용", example = "이 일정에 참여하고 싶습니다!")
    val content: String
)
