package net.noti_me.dymit.dymit_backend_api.domain.study_group

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("study_group_members")
class StudyGroupMember(
    @Id
    val id: ObjectId = ObjectId.get(),
    groupId: ObjectId = ObjectId.get(),
    memberId: ObjectId = ObjectId.get(),
    nickname: String = "",
    profileImage: MemberProfileImageVo,
    role: GroupMemberRole = GroupMemberRole.MEMBER,
): BaseAggregateRoot<StudyGroupMember>() {

    val identifier: String
        get() = id.toHexString()

    val groupId: ObjectId = groupId

    val memberId: ObjectId = memberId

    var nickname: String = nickname
        private set;

    var profileImage = profileImage
        private set;

    var role: GroupMemberRole = role
        private set;

    fun updateProfileImage(newProfileImage: MemberProfileImageVo) {
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
}