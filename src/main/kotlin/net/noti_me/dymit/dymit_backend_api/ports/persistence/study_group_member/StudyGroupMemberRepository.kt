package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroupMember

interface StudyGroupMemberRepository {

    fun persist(member: StudyGroupMember): StudyGroupMember

    fun update(member: StudyGroupMember): StudyGroupMember

    fun delete(member: StudyGroupMember): Boolean

    fun findByGroupIdAndMemberId(groupId: String, memberId: String): StudyGroupMember?

    fun countByGroupId(groupId: String): Long

    fun findByGroupIdsOrderByCreatedAt(groupIds: List<String>, limit: Int): Map<String, List<StudyGroupMember>>

    fun findGroupIdsByMemberId(memberId: String): List<String>
}