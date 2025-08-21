package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroupMember
import java.time.LocalDateTime

class StudyGroupMemberQueryDto(
    val groupId: String,
    val memberId: String,
    val nickname: String,
    val role: GroupMemberRole,
    var profileImage: MemberProfileImageVo,
    val createdAt: LocalDateTime,
) {

    companion object {

        fun from(entity: StudyGroupMember): StudyGroupMemberQueryDto {
            return StudyGroupMemberQueryDto(
                groupId = entity.groupId.toHexString(),
                memberId = entity.memberId.toHexString(),
                nickname = entity.nickname,
                role = entity.role,
                profileImage = entity.profileImage,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}