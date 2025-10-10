package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class StudyGroupEventHandler(
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener(classes = [MemberProfileImageChangedEvent::class] )
    @Async
    fun handleMemberProfileImageChangedEvent(event: MemberProfileImageChangedEvent) {
        val member = event.member
        val profileImage = ProfileImageVo.from(member.profileImage)
        val memberId = member.id!!
        var groupMembers = studyGroupMemberRepository.findByMemberId(
            memberId = memberId,
            cursor = null,
            limit = DEFAULT_BATCH_SIZE + 1
        )
        var cursor: ObjectId? = null

        do {
            val targets = groupMembers
                .take(DEFAULT_BATCH_SIZE)
            targets.forEach { it.updateProfileImage(profileImage) }
            studyGroupMemberRepository.saveAll(targets)
            cursor = targets.lastOrNull()?.id

            groupMembers = if (groupMembers.size > DEFAULT_BATCH_SIZE) {
                studyGroupMemberRepository.findByMemberId(
                    memberId = memberId,
                    cursor = cursor,
                    limit = DEFAULT_BATCH_SIZE + 1
                )
            } else {
                emptyList()
            }
        } while ( groupMembers.isNotEmpty() )
    }

    companion object {
        private const val DEFAULT_BATCH_SIZE = 50
    }
}