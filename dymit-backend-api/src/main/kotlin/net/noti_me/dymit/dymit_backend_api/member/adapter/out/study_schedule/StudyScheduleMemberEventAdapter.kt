package net.noti_me.dymit.dymit_backend_api.member.adapter.out.study_schedule

import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberNicknameChangedEvent
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleMemberEventPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleMemberEventDto
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class StudyScheduleMemberEventAdapter(
    private val memberEventPort: StudyScheduleMemberEventPort
) {

    @Async
    @EventListener
    fun onMemberNicknameChanged(event: MemberNicknameChangedEvent) {
        memberEventPort.memberNicknameChanged(event.member.toScheduleEventDto())
    }

    @Async
    @EventListener
    fun onMemberProfileImageChanged(event: MemberProfileImageChangedEvent) {
        memberEventPort.memberProfileImageChanged(event.member.toScheduleEventDto())
    }

    private fun Member.toScheduleEventDto(): StudyScheduleMemberEventDto {
        return StudyScheduleMemberEventDto.of(
            memberId = identifier,
            nickname = nickname,
            profileImageType = profileImage.type.name,
            profileImageUrl = profileImage.thumbnail
        )
    }
}
