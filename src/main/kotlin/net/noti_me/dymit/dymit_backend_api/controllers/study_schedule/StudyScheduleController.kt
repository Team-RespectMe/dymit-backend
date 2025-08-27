package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.StudyScheduleService
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleParticipantResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleListItem
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class StudyScheduleController(
    private val scheduleService: StudyScheduleService
) : StudyScheduleApi {

    override fun createSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        request: StudyScheduleCommandRequest
    ): StudyScheduleCommandResponse {
        val dto = scheduleService.createSchedule(memberInfo, groupId, request.toCreateCommand())
        return StudyScheduleCommandResponse.from(dto)
    }

    override fun updateSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        command: StudyScheduleCommandRequest
    ): StudyScheduleCommandResponse {
        val dto = scheduleService.updateSchedule(memberInfo, groupId, scheduleId, command.toUpdateCommand())
        return StudyScheduleCommandResponse.from(dto)
    }

    override fun removeSchedule(memberInfo: MemberInfo, groupId: String, scheduleId: String) {
        scheduleService.removeSchedule(memberInfo, groupId, scheduleId)
    }

    override fun joinSchedule(memberInfo: MemberInfo, groupId: String, scheduleId: String): ScheduleParticipantResponse {
        val participant = scheduleService.joinSchedule(memberInfo, groupId, scheduleId)
        return ScheduleParticipantResponse.from(participant)
    }

    override fun leaveSchedule(memberInfo: MemberInfo, groupId: String, scheduleId: String) {
        scheduleService.leaveSchedule(memberInfo, groupId, scheduleId)
    }

    override fun getScheduleDetail(memberInfo: MemberInfo, groupId: String, scheduleId: String): StudyScheduleResponse {
        val dto = scheduleService.getScheduleDetail(memberInfo, groupId, scheduleId)
        return StudyScheduleResponse.from(dto)
    }

    override fun getGroupSchedules(memberInfo: MemberInfo, groupId: String): ListResponse<StudyScheduleListItem> {
        val response = scheduleService.getGroupSchedules(memberInfo, groupId)
        return ListResponse.from(response.map { StudyScheduleListItem.from(it) })
    }
}