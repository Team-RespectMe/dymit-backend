package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType

data class StudyScheduleMemberEventDto(
    val memberId: String,
    val nickname: String,
    val profileImageType: ProfileImageType,
    val profileImageUrl: String
)
