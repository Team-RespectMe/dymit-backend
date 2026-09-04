package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query

import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import java.time.Instant

class StudyGroupMemberQueryDto(
    val groupId: String,
    val memberId: String,
    val nickname: String,
    val role: GroupMemberRole,
    var profileImage: ProfileImageVo,
    val createdAt: Instant,
) {

    companion object {

        fun from(entity: StudyGroupMember): StudyGroupMemberQueryDto {
            return StudyGroupMemberQueryDto(
                groupId = entity.groupId.toHexString(),
                memberId = entity.memberId.toHexString(),
                nickname = entity.nickname,
                role = entity.role,
                profileImage = entity.profileImage,
                createdAt = entity.createdAt ?: Instant.now()
            )
        }
    }
}
