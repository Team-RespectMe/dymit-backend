package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command

import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import java.time.Instant

class StudyGroupDto(
    val groupId: String,
    val profileImage: GroupProfileImageVo,
    var ownerId: String,
    val name: String,
    val description: String,
    val inviteCodeVo: InviteCodeVo,
    val createdAt: Instant
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
                createdAt = entity.createdAt ?: Instant.now()
            )
        }
    }
}
