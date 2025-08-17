package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command

import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroupMember
import java.time.LocalDateTime

class StudyGroupMemberDto(
    val groupId: String,
    val memberId: String,
    val nickname: String,
    val profileImageVo: MemberProfileImageVo,
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

        fun dummy(): StudyGroupMemberDto {
            return StudyGroupMemberDto(
                groupId = "groupId",
                memberId = "memberId",
                nickname = "nickname",
                profileImageVo = MemberProfileImageVo(),
                role = GroupMemberRole.MEMBER,
                createdAt = LocalDateTime.now()
            )
        }
    }
}