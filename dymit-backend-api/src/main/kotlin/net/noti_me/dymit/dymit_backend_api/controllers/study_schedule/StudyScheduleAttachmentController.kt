package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.StudyScheduleAttachmentService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleAttachmentReplaceRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleAttachmentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 스터디 일정 첨부 파일 컨트롤러입니다.
 *
 * @param studyScheduleAttachmentService 스터디 일정 첨부 파일 서비스
 */
@RestController
@RequestMapping("/api/v1/study-schedules")
class StudyScheduleAttachmentController(
    private val studyScheduleAttachmentService: StudyScheduleAttachmentService
) : StudyScheduleAttachmentApi {

    @PutMapping("/{scheduleId}/attachments")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun replaceAttachments(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable scheduleId: String,
        @RequestBody @Valid @Sanitize request: StudyScheduleAttachmentReplaceRequest
    ): ListResponse<StudyScheduleAttachmentResponse> {
        val attachments = studyScheduleAttachmentService.replaceAttachments(
            memberInfo = memberInfo,
            command = request.toCommand(scheduleId)
        )
        return ListResponse.from(attachments.map { StudyScheduleAttachmentResponse.from(it) })
    }

    @GetMapping("/{scheduleId}/attachments")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getAttachments(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable scheduleId: String
    ): ListResponse<StudyScheduleAttachmentResponse> {
        val attachments = studyScheduleAttachmentService.getAttachments(
            memberInfo = memberInfo,
            scheduleId = scheduleId
        )
        return ListResponse.from(attachments.map { StudyScheduleAttachmentResponse.from(it) })
    }
}
