package net.noti_me.dymit.dymit_backend_api.member.adapter.out.study_group

import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberCreatedEvent
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberDeletedEvent
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberForceDeletedEvent
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberNicknameChangedEvent
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberEventPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberEventDto
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class StudyGroupMemberEventAdapter(
    private val studyGroupMemberEventPort: StudyGroupMemberEventPort
) {

    @Async
    @EventListener
    fun onMemberCreated(event: MemberCreatedEvent) {
        studyGroupMemberEventPort.memberCreated(event.member.toStudyGroupEventDto())
    }

    @Async
    @EventListener
    fun onMemberDeleted(event: MemberDeletedEvent) {
        studyGroupMemberEventPort.memberDeleted(event.member.toStudyGroupEventDto())
    }

    @Async
    @EventListener
    fun onMemberForceDeleted(event: MemberForceDeletedEvent) {
        studyGroupMemberEventPort.memberForceDeleted(event.member.identifier)
    }

    @Async
    @EventListener
    fun onMemberNicknameChanged(event: MemberNicknameChangedEvent) {
        studyGroupMemberEventPort.memberNicknameChanged(
            memberId = event.member.identifier,
            nickname = event.member.nickname
        )
    }

    @Async
    @EventListener
    fun onMemberProfileImageChanged(event: MemberProfileImageChangedEvent) {
        studyGroupMemberEventPort.memberProfileImageChanged(event.member.toStudyGroupEventDto())
    }

    private fun Member.toStudyGroupEventDto() = StudyGroupMemberEventDto(
        memberId = identifier,
        nickname = nickname,
        roles = roles.map { it.name },
        profileImageType = profileImage.type,
        profileImageUrl = profileImage.thumbnail
    )
}
