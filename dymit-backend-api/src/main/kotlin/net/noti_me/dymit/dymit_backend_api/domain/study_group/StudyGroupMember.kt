package net.noti_me.dymit.dymit_backend_api.domain.study_group

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("study_group_members")
class StudyGroupMember(
//    @Id
//    val id: ObjectId = ObjectId.get(),
    id: ObjectId? = null,
    groupId: ObjectId = ObjectId.get(),
    memberId: ObjectId = ObjectId.get(),
    nickname: String = "",
    profileImage: ProfileImageVo = ProfileImageVo(),
    role: GroupMemberRole = GroupMemberRole.MEMBER,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
): BaseAggregateRoot<StudyGroupMember>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

//    val identifier: String
//        get() = id.toHexString()

    val groupId: ObjectId = groupId

    val memberId: ObjectId = memberId

    var nickname: String = nickname
        private set;

    var profileImage = profileImage
        private set;

    var role: GroupMemberRole = role
        private set;

    fun updateProfileImage(newProfileImage: ProfileImageVo) {
        this.profileImage = newProfileImage
    }

    fun updateNickname(newNickname: String) {
        if (newNickname.isBlank()) {
            throw IllegalArgumentException("Nickname cannot be blank")
        }
        this.nickname = newNickname
    }

    fun changeRole(requester: StudyGroupMember, newRole: GroupMemberRole) {
        if (requester.role != GroupMemberRole.OWNER) {
            throw ForbiddenException(message = "스터디 그룹 소유자만 멤버의 역할을 변경할 수 있습니다.")
        }
        this.role = newRole
    }

    fun forcePromote(role: GroupMemberRole) {
        this.role = role
    }
}
