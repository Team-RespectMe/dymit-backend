package net.noti_me.dymit.dymit_backend_api.units.member.adapter.out.study_group

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.adapter.out.study_group.StudyGroupMemberEventAdapter
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberCreatedEvent
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberForceDeletedEvent
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberEventPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberEventDto
import org.bson.types.ObjectId

internal class StudyGroupMemberEventAdapterTest : BehaviorSpec() {

    private val eventPort = mockk<StudyGroupMemberEventPort>(relaxed = true)
    private val adapter = StudyGroupMemberEventAdapter(eventPort)

    init {
        given("a member event") {
            val member = Member(
                id = ObjectId.get(),
                nickname = "member",
                profileImage = MemberProfileImageVo(
                    type = ProfileImageType.EXTERNAL,
                    thumbnail = "thumbnail",
                    original = "original"
                ),
                roles = mutableSetOf(MemberRole.ROLE_MEMBER)
            )

            `when`("the member adapter forwards a creation event") {
                then("it calls only the study-group inbound port with its DTO") {
                    val dto = slot<StudyGroupMemberEventDto>()
                    every { eventPort.memberCreated(capture(dto)) } returns Unit

                    adapter.onMemberCreated(MemberCreatedEvent(member))

                    verify(exactly = 1) { eventPort.memberCreated(any()) }
                    dto.captured.memberId shouldBe member.identifier
                    dto.captured.nickname shouldBe member.nickname
                    dto.captured.roles shouldBe listOf(MemberRole.ROLE_MEMBER.name)
                    dto.captured.profileImageType shouldBe ProfileImageType.EXTERNAL
                    dto.captured.profileImageUrl shouldBe "thumbnail"
                }
            }

            `when`("the member adapter forwards a force-deletion event") {
                then("it passes only the member identifier through the inbound port") {
                    adapter.onMemberForceDeleted(MemberForceDeletedEvent(member))

                    verify(exactly = 1) { eventPort.memberForceDeleted(member.identifier) }
                }
            }
        }
    }
}
