package net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleCreateCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleDetailDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleParticipantDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleSummaryDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface StudyScheduleService {

    fun createSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        command: StudyScheduleCreateCommand
    ): StudyScheduleDto

    fun updateSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        command: StudyScheduleUpdateCommand
    ): StudyScheduleDto

    fun removeSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): Unit

    fun getGroupSchedules(
        memberInfo: MemberInfo,
        groupId: String
    ): List<StudyScheduleSummaryDto>

    fun getScheduleDetail(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): StudyScheduleDetailDto

    fun joinSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): StudyScheduleParticipantDto

    fun leaveSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): Unit
}
