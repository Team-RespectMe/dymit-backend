package net.noti_me.dymit.dymit_backend_api.application.study_schedule

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleDetailDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleParticipantDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleSummaryDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

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