package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType

data class StudyScheduleProfileImageResponse(
    val type: ProfileImageType,
    val url: String
) {

    companion object {
        fun of(type: ProfileImageType, url: String): StudyScheduleProfileImageResponse {
            return StudyScheduleProfileImageResponse(type = type, url = url)
        }
    }
}
