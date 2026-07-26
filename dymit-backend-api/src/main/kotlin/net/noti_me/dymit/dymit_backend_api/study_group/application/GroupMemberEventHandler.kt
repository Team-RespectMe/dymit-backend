package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.GroupOwnerMissingEvent
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
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
