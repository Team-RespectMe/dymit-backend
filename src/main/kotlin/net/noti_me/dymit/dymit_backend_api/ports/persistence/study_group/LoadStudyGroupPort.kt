package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroup

interface LoadStudyGroupPort {

    fun loadByInviteCode(inviteCode: String): StudyGroup?

    fun loadByOwnerId(ownerId: String): List<StudyGroup>

    fun loadByGroupId(groupId: String): StudyGroup?

    fun loadByGroupIds(groupIds: List<String>): List<StudyGroup>

    fun existsByInviteCode(inviteCode: String): Boolean
}