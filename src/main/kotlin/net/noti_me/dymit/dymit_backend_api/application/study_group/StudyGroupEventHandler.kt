package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.GroupMemberBlacklistedEvent
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.event.CreateUserFeedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
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

    companion object {
        private const val DEFAULT_BATCH_SIZE = 50
    }

    @EventListener
    @Async
    fun handleMemberProfileImageChangedEvent(event: MemberProfileImageChangedEvent) {
        logger.debug("handleMemberProfileImageChangedEvent: $event")
        val member = event.member
        // 멤버의 프로필 이미지가 변경되었을 때, 관련된 스터디 그룹 멤버 정보도 업데이트
        var studyGroupMembers = studyGroupMemberRepository.findByMemberId(
            memberId = member.id!!,
            cursor = null,
            limit = DEFAULT_BATCH_SIZE + 1
        )

        while ( studyGroupMembers.isNotEmpty() ) {
            studyGroupMembers.forEach { sm ->
                sm.updateProfileImage(
                    newProfileImage = member.profileImage
                )
            }
            studyGroupMemberRepository.saveAll(studyGroupMembers)
            if (studyGroupMembers.size <= DEFAULT_BATCH_SIZE) {
                break
            }

            val lastMember = studyGroupMembers.last()
            studyGroupMembers = studyGroupMemberRepository.findByMemberId(
                memberId = member.id,
                cursor = lastMember.id,
                limit = DEFAULT_BATCH_SIZE + 1
            )
        }
    }

    @EventListener
    @Async
    fun handleGroupMemberBlacklistedEvent(event: GroupMemberBlacklistedEvent) {
        val blacklisted = event.blacklisted
        // 블랙리스트에 추가된 멤버가 스터디 그룹에서 탈퇴 처리

        val studyGroupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId= event.group.id!!,
            memberId = blacklisted.memberId
        ) ?: return

        studyGroupMemberRepository.delete(studyGroupMember)
        eventPublisher.publishEvent(
            CreateUserFeedEvent(
                memberId = studyGroupMember.memberId,
                content = "${event.group.name} 스터디 그룹에서 강제 탈퇴되었습니다."
            )
        )
    }
}