package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudyScheduleProfileImageType

data class StudyScheduleProfileImageResponse(
    val type: StudyScheduleProfileImageType,
    val url: String
) {

    companion object {
        fun of(type: StudyScheduleProfileImageType, url: String): StudyScheduleProfileImageResponse {
            return StudyScheduleProfileImageResponse(type = type, url = url)
        }
    }
}
