package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.bson.types.ObjectId

interface StudyGroupMemberRepository {

    fun saveAll(members: List<StudyGroupMember>): List<StudyGroupMember>

    fun persist(member: StudyGroupMember): StudyGroupMember

    fun update(member: StudyGroupMember): StudyGroupMember

    fun delete(member: StudyGroupMember): Boolean

    fun findByMemberId(memberId: ObjectId, cursor: ObjectId?, limit: Int): List<StudyGroupMember>

    fun findByGroupIdAndMemberId(groupId: ObjectId, memberId: ObjectId): StudyGroupMember?

    fun findByIdIncludingDeleted(membershipId: ObjectId): StudyGroupMember?

    fun countByGroupId(groupId: ObjectId): Long

    fun findByGroupId(groupId: ObjectId): List<StudyGroupMember>

    fun findByGroupIdIncludingDeleted(
        groupId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyGroupMember>

    fun findActiveByGroupId(
        groupId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyGroupMember>

    fun findByGroupIdsOrderByCreatedAt(groupIds: List<ObjectId>, limit: Int): Map<String, List<StudyGroupMember>>

    fun findGroupIdsByMemberId(memberId: ObjectId): List<String>

    fun findByGroupIdAndMemberIdsIn(groupId: ObjectId, memberIds: List<ObjectId>): List<StudyGroupMember>

    fun countByMemberIdAndRole(memberId: ObjectId, role: GroupMemberRole): Long

    /** 현재 소유자 또는 관리자인 활성 그룹 ID를 커서 순서로 조회합니다. */
    fun findManagedGroupIds(
        memberId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<ObjectId>

    /** 그룹별 현재 활성 사용자 수를 중복 없이 집계합니다. */
    fun countDistinctActiveMembers(groupIds: List<ObjectId>): Map<ObjectId, Long>
}
