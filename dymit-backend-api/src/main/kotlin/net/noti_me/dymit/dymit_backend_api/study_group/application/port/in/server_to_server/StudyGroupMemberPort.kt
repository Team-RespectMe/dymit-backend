package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberRoleDto
import org.bson.types.ObjectId

interface StudyGroupMemberPort {

    fun findByMemberId(memberId: ObjectId, cursor: ObjectId?, limit: Int): List<StudyGroupMemberDto>

    fun findByGroupIdAndMemberId(groupId: ObjectId, memberId: ObjectId): StudyGroupMemberDto?

    fun countByGroupId(groupId: ObjectId): Long

    fun findByGroupId(groupId: ObjectId): List<StudyGroupMemberDto>

    fun findByGroupIdsOrderByCreatedAt(
        groupIds: List<ObjectId>,
        limit: Int
    ): Map<String, List<StudyGroupMemberDto>>

    fun findGroupIdsByMemberId(memberId: ObjectId): List<String>

    fun findByGroupIdAndMemberIdsIn(
        groupId: ObjectId,
        memberIds: List<ObjectId>
    ): List<StudyGroupMemberDto>

    fun countByMemberIdAndRole(memberId: ObjectId, role: StudyGroupMemberRoleDto): Long
}
