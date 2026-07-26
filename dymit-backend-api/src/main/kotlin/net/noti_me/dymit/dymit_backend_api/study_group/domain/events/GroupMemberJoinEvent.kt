package net.noti_me.dymit.dymit_backend_api.study_group.domain.events

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.bson.types.ObjectId

data class GroupMemberJoinEvent(
    val groupId: String,
    val groupName: String,
    val ownerId: ObjectId,
    val memberId: ObjectId,
    val memberNickname: String
) {

    companion object {
        const val EVENT_NAME = "GROUP_MEMBER_JOIN"

        fun of(
            group: StudyGroup,
            member: StudyGroupMember
        ) = GroupMemberJoinEvent(
            groupId = group.identifier,
            groupName = group.name,
            ownerId = group.ownerId,
            memberId = member.memberId,
            memberNickname = member.nickname
        )
    }
}
