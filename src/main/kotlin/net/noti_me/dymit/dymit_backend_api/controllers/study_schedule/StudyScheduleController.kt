package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.StudyScheduleService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/study-groups/")
class StudyScheduleController(
    private val scheduleService: StudyScheduleService
) : StudyScheduleApi {

    @PostMapping("{groupId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun createSchedule(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid @Sanitize request: StudyScheduleCommandRequest
    ): StudyScheduleCommandResponse {
        val dto = scheduleService.createSchedule(memberInfo, groupId, request.toCreateCommand())
        return StudyScheduleCommandResponse.from(dto)
    }

    @PutMapping("{groupId}/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun updateSchedule(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String,
        @RequestBody @Valid @Sanitize request: StudyScheduleCommandRequest
    ): StudyScheduleCommandResponse {
        val dto = scheduleService.updateSchedule(memberInfo, groupId, scheduleId, request.toUpdateCommand())
        return StudyScheduleCommandResponse.from(dto)
    }

    @DeleteMapping("{groupId}/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun removeSchedule(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String
    ) {
        scheduleService.removeSchedule(memberInfo, groupId, scheduleId)
    }

    @GetMapping("/{groupId}/schedules")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getGroupSchedules(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): ListResponse<StudyScheduleListItem> {
        val response = scheduleService.getGroupSchedules(memberInfo, groupId)
        return ListResponse.from(response.map { StudyScheduleListItem.from(it) })
    }

    @GetMapping("{groupId}/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getScheduleDetail(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String
    ): StudyScheduleResponse {
        val dto = scheduleService.getScheduleDetail(memberInfo, groupId, scheduleId)
        return StudyScheduleResponse.from(dto)
    }

    @PostMapping("{groupId}/schedules/{scheduleId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun joinSchedule(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String
    ): ScheduleParticipantResponse {
        val participant = scheduleService.joinSchedule(memberInfo, groupId, scheduleId)
        return ScheduleParticipantResponse.from(participant)
    }

    @DeleteMapping("{groupId}/schedules/{scheduleId}/participants")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun leaveSchedule(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable scheduleId: String
    ){
        scheduleService.leaveSchedule(memberInfo, groupId, scheduleId)
    }


}