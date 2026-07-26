package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member.dto

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType
import java.time.LocalDateTime

data class StudyGroupMemberData(
    val id: String,
    val nickname: String,
    val profileImageType: StudyGroupProfileImageType,
    val profileImageThumbnail: String,
    val profileImageOriginal: String,
    val roles: List<String>,
    val createdAt: LocalDateTime?
)
