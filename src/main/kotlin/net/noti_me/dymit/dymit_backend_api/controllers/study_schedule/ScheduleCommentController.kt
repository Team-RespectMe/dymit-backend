package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.ScheduleCommentService
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.CreateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.UpdateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleCommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleCommentResponse
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/study-groups")
class ScheduleCommentController(
    private val scheduleCommentService: ScheduleCommentService
) : ScheduleCommentApi {

    @PostMapping("/{groupId}/schedules/{scheduleId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun createComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @RequestBody @Valid @Sanitize request: ScheduleCommentCommandRequest
    ): ScheduleCommentResponse {
        val command = CreateScheduleCommentCommand(
            groupId = ObjectId(groupId),
            scheduleId = ObjectId(scheduleId),
            content = request.content
        )

        val dto = scheduleCommentService.createComment(memberInfo, command)
        return ScheduleCommentResponse.from(dto)
    }

    @PutMapping("/{groupId}/schedules/{scheduleId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun updateComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @PathVariable commentId: String,
        @RequestBody @Valid @Sanitize request: ScheduleCommentCommandRequest
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

    @DeleteMapping("/{groupId}/schedules/{scheduleId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun deleteComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @PathVariable commentId: String
    ) {
        scheduleCommentService.deleteComment(memberInfo, commentId)
    }

    @GetMapping("/{groupId}/schedules/{scheduleId}/comments")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getScheduleComments(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int
    ): ListResponse<ScheduleCommentResponse> {
        // size+1로 조회하여 다음 페이지 존재 여부 확인
        val comments = scheduleCommentService.getScheduleComments(
            memberInfo = memberInfo,
            scheduleId = scheduleId,
            cursor = cursor,
            size = size + 1
        )
        val responseComments = comments.map { ScheduleCommentResponse.from(it) }
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
