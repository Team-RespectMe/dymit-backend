package net.noti_me.dymit.dymit_backend_api.domain.studyGroup

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("study_group_members")
class StudyGroupMember(
    @Id
    val id: ObjectId = ObjectId.get(),
    groupId: String = "",
    memberId: String = "",
    nickname: String = "",
    profileImage: MemberProfileImageVo,
    role: GroupMemberRole = GroupMemberRole.MEMBER,
): BaseAggregateRoot<StudyGroupMember>() {

    val identifier: String
        get() = id.toHexString()

    val groupId: String = groupId

    val memberId: String = memberId

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

    fun changeRole(newRole: GroupMemberRole) {
        if (newRole == GroupMemberRole.OWNER) {
            throw IllegalArgumentException("Cannot change role to OWNER directly")
        }
        this.role = newRole
    }
}