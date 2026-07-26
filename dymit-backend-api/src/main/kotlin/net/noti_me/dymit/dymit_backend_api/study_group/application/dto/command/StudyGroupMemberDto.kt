package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command

import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import java.time.LocalDateTime

class StudyGroupMemberDto(
    val groupId: String,
    val memberId: String,
    val nickname: String,
    val profileImageVo: ProfileImageVo,
    val role: GroupMemberRole,
    val createdAt: LocalDateTime,
) {

    companion object {

        fun from(
            entity: StudyGroupMember
        ): StudyGroupMemberDto {
            return StudyGroupMemberDto(
                groupId = entity.groupId.toHexString(),
                memberId = entity.memberId.toHexString(),
                nickname = entity.nickname,
                profileImageVo = entity.profileImage,
                role = entity.role,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}
