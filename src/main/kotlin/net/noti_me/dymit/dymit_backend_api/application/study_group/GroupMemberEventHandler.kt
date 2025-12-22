package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.GroupOwnerMissingEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class GroupMemberEventHandler(
    private val saveStudyGroupPort: SaveStudyGroupPort,
    private val groupMemberRepository: StudyGroupMemberRepository
) {

    @Async
    @EventListener(classes = [GroupOwnerMissingEvent::class])
    fun onGroupOwnerMissing(event: GroupOwnerMissingEvent) {
        val groupId = event.group.id!!
        val groupMembers = groupMemberRepository.findByGroupId(groupId)
        val candidate = groupMembers.filter { member->member.role != GroupMemberRole.OWNER }
            .sortedByDescending { it.role }
            .firstOrNull() ?: return
        event.group.promoteOwner(candidate)
        saveStudyGroupPort.update(event.group)
        groupMemberRepository.update(candidate)
    }
}