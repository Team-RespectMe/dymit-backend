package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType

data class StudyGroupMemberEventDto(
    val memberId: String,
    val nickname: String,
    val roles: List<String>,
    val profileImageType: ProfileImageType = ProfileImageType.PRESET,
    val profileImageUrl: String = ""
)
