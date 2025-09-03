package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member

import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import org.bson.types.ObjectId

interface StudyGroupMemberRepository {

    fun saveAll(members: List<StudyGroupMember>): List<StudyGroupMember>

    fun persist(member: StudyGroupMember): StudyGroupMember

    fun update(member: StudyGroupMember): StudyGroupMember

    fun delete(member: StudyGroupMember): Boolean

    fun findByMemberId(memberId: ObjectId, cursor: ObjectId?, limit: Int): List<StudyGroupMember>

    fun findByGroupIdAndMemberId(groupId: ObjectId, memberId: ObjectId): StudyGroupMember?

    fun countByGroupId(groupId: ObjectId): Long

    fun findByGroupId(groupId: ObjectId): List<StudyGroupMember>

    fun findByGroupIdsOrderByCreatedAt(groupIds: List<ObjectId>, limit: Int): Map<String, List<StudyGroupMember>>

    fun findGroupIdsByMemberId(memberId: ObjectId): List<String>

    fun findByGroupIdAndMemberIdsIn(groupId: ObjectId, memberIds: List<ObjectId>): List<StudyGroupMember>
}