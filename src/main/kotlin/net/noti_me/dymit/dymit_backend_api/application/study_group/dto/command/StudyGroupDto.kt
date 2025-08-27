package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import java.time.LocalDateTime

class StudyGroupDto(
    val groupId: String,
    val profileImage: GroupProfileImageVo,
    var ownerId: String,
    val name: String,
    val description: String,
    val inviteCodeVo: InviteCodeVo,
    val createdAt: LocalDateTime
) {

    companion object {
        fun fromEntity(entity: StudyGroup): StudyGroupDto {
            return StudyGroupDto(
                groupId = entity.identifier,
                profileImage =  entity.profileImage,
                ownerId = entity.ownerId.toHexString(),
                name = entity.name,
                description = entity.description,
                inviteCodeVo = entity.inviteCode,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}