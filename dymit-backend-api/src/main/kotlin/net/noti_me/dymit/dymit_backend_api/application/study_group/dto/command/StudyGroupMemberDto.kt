package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command

import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
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