package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import java.time.LocalDateTime

data class SchedulePreview(
    val title: String,
    val sessionNumber: Int,
    val location: String,
    val startAt: LocalDateTime
) {

}