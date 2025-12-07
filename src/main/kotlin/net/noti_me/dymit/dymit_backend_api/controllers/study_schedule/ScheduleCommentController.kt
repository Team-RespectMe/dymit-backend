package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.ScheduleCommentService
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.CreateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.UpdateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleCommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleCommentResponse
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.RestController

@RestController
class ScheduleCommentController(
    private val scheduleCommentService: ScheduleCommentService
) : ScheduleCommentApi {

    override fun createComment(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        @Valid @Sanitize request: ScheduleCommentCommandRequest
    ): ScheduleCommentResponse {
        val command = CreateScheduleCommentCommand(
            groupId = ObjectId(groupId),
            scheduleId = ObjectId(scheduleId),
            content = request.content
        )

        val dto = scheduleCommentService.createComment(memberInfo, command)
        return ScheduleCommentResponse.from(dto)
    }

    override fun updateComment(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        commentId: String,
        @Valid @Sanitize request: ScheduleCommentCommandRequest
    ): ScheduleCommentResponse {
        val command = UpdateScheduleCommentCommand(
            groupId = ObjectId(groupId),
            scheduleId = ObjectId(scheduleId),
            commentId = ObjectId(commentId),
            content = request.content
        )

        val dto = scheduleCommentService.updateComment(memberInfo, command)
        return ScheduleCommentResponse.from(dto)
    }

    override fun deleteComment(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        commentId: String
    ) {
        scheduleCommentService.deleteComment(memberInfo, commentId)
    }

    override fun getScheduleComments(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        cursor: String?,
        size: Int
    ): ListResponse<ScheduleCommentResponse> {
        // size+1로 조회하여 다음 페이지 존재 여부 확인
        val comments = scheduleCommentService.getScheduleComments(
            memberInfo = memberInfo,
            scheduleId = scheduleId,
            cursor = cursor,
            size = size + 1
        )

        // ScheduleCommentDto를 ScheduleCommentResponse로 변환
        val responseComments = comments.map { ScheduleCommentResponse.from(it) }

        // ListResponse.of() 메서드를 사용하여 커서 페이지네이션 적용
        return ListResponse.of(
            size = size,
            items = responseComments,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { size }
            }
        )
    }
}
