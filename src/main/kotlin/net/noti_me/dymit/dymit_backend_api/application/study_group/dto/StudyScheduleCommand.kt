package net.noti_me.dymit.dymit_backend_api.application.study_group.dto

import java.time.LocalDateTime

class StudyScheduleCommand(
    val groupId: String,
    val title: String,
    val description: String? = null,
    val scheduleAt: LocalDateTime= LocalDateTime.now(),
) {
}