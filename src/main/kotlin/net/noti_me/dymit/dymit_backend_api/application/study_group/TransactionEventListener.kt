package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberDeletedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.GroupOwnerMissingEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalEventPublisher

@Service
class TransactionEventListener(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val studyGroupCommandService: StudyGroupCommandService,
) {

    companion object {
        private const val FETCH_GROUP_MEMBER_LIMITS = 100
    }

    @Async
    @EventListener(classes = [MemberDeletedEvent::class])
    fun onMemberLeaved(event: MemberDeletedEvent) {
        var cursor: ObjectId? = null
        do {
            val groupMembers = studyGroupMemberRepository.findByMemberId(
                memberId = event.member.id!!,
                cursor = cursor,
                limit = FETCH_GROUP_MEMBER_LIMITS + 1
            )


            groupMembers.forEach { groupMember ->
                studyGroupCommandService.leaveStudyGroup(
                    member = MemberInfo(
                        memberId = event.member.identifier,
                        nickname = event.member.nickname,
                        roles = emptyList()
                    ),
                    groupId = groupMember.groupId.toHexString(),
                )
            }

            if ( groupMembers.size > FETCH_GROUP_MEMBER_LIMITS ) {
                cursor = groupMembers.last().id
            }
        } while( groupMembers.size > FETCH_GROUP_MEMBER_LIMITS) 
    }
}

