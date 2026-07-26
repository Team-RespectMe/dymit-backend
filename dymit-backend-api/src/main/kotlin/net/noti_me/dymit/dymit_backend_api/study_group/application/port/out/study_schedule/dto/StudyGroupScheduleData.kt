package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.study_schedule.dto

import java.time.LocalDateTime

data class StudyGroupScheduleData(
    val id: String,
    val groupId: String,
    val title: String,
    val session: Long,
    val scheduleAt: LocalDateTime
)
