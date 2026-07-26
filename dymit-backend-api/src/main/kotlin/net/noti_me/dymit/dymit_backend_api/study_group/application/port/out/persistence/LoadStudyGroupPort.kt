package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup

interface LoadStudyGroupPort {

    fun loadByInviteCode(inviteCode: String): StudyGroup?

    fun loadByOwnerId(ownerId: String): List<StudyGroup>

    fun loadByGroupId(groupId: String): StudyGroup?

    fun loadByGroupIds(groupIds: List<String>): List<StudyGroup>

    fun countByOwnerId(ownerId: String): Long

    fun existsByInviteCode(inviteCode: String): Boolean
}