package net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.CreateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.ScheduleCommentDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.UpdateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleComment

interface ScheduleCommentService {

    fun createComment(
        memberInfo: MemberInfo,
        command: CreateScheduleCommentCommand
    ): ScheduleCommentDto

    fun updateComment(
        memberInfo: MemberInfo,
        command: UpdateScheduleCommentCommand
    ): ScheduleCommentDto

    fun deleteComment(
        memberInfo: MemberInfo,
        commentId: String
    )

    fun getScheduleComments(
        memberInfo: MemberInfo,
        scheduleId: String,
        cursor: String? = null,
        size: Int = 20
    ): List<ScheduleCommentDto>
}
