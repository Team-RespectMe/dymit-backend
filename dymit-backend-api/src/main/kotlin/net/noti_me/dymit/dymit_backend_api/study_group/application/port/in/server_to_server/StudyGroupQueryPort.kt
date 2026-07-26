package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto

interface StudyGroupQueryPort {

    fun loadByInviteCode(inviteCode: String): StudyGroupDto?

    fun loadByOwnerId(ownerId: String): List<StudyGroupDto>

    fun loadByGroupId(groupId: String): StudyGroupDto?

    fun loadByGroupIds(groupIds: List<String>): List<StudyGroupDto>

    fun countByOwnerId(ownerId: String): Long

    fun existsByInviteCode(inviteCode: String): Boolean
}
