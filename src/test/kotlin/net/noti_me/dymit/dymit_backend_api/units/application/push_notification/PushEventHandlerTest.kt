package net.noti_me.dymit.dymit_backend_api.units.application.push_notification

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.push_notification.GroupBroadCastPushEvent
import net.noti_me.dymit.dymit_backend_api.application.push_notification.MemberPushEvent
import net.noti_me.dymit.dymit_backend_api.application.push_notification.PushService
import net.noti_me.dymit.dymit_backend_api.application.push_notification.SchedulePushEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

/**
 * PushEventHandler 클래스의 이벤트 처리 기능을 테스트한다.
 */
class PushEventHandlerTest : BehaviorSpec({

    val pushService = mockk<PushService>()
    val loadMemberPort = mockk<LoadMemberPort>()
    val groupMemberRepository = mockk<StudyGroupMemberRepository>()
    val participantRepository = mockk<ScheduleParticipantRepository>()
    val eventPublisher = mockk<ApplicationEventPublisher>()
    val pushEventHandler = PushEventHandler(
        pushService,
        loadMemberPort,
        groupMemberRepository,
        participantRepository,
        eventPublisher
    )

    val testGroupId = ObjectId()
    val testScheduleId = ObjectId()
    val testMemberId = ObjectId()

    // 테스트용 Member 생성 함수
    fun createTestMember(memberId: ObjectId, deviceTokens: Set<String>): Member {
        return Member(
            id = memberId,
            nickname = "testMember",
            oidcIdentities = mutableSetOf(),
            profileImage = MemberProfileImageVo(
                type = "preset",
                filePath = "",
                url = "0",
                fileSize = 0L,
                width = 0,
                height = 0
            ),
            lastAccessAt = LocalDateTime.now(),
            deviceTokens = deviceTokens.map { DeviceToken(it) }.toMutableSet(),
            refreshTokens = mutableSetOf()
        )
    }

    // 테스트용 StudyGroupMember 생성 함수
    fun createTestGroupMember(groupId: ObjectId, memberId: ObjectId): StudyGroupMember {
        return StudyGroupMember(
            id = ObjectId(),
            groupId = groupId,
            memberId = memberId,
            nickname = "testMember",
            profileImage = MemberProfileImageVo(
                type = "preset",
                filePath = "",
                url = "0",
                fileSize = 0L,
                width = 0,
                height = 0
            ),
            role = GroupMemberRole.MEMBER
        )
    }

    // 테스트용 ScheduleParticipant 생성 함수
    fun createTestParticipant(scheduleId: ObjectId, memberId: ObjectId): ScheduleParticipant {
        return ScheduleParticipant(
            id = ObjectId(),
            scheduleId = scheduleId,
            memberId = memberId
        )
    }

    given("그룹 브로드캐스트 푸시 이벤트가 발생한 상황에서") {
        val event = GroupBroadCastPushEvent(
            groupId = testGroupId,
            title = "그룹 알림",
            body = "새로운 공지사항이 있습니다.",
            image = "https://example.com/image.jpg",
            data = mapOf("type" to "announcement")
        )

        `when`("그룹에 디바이스 토큰을 가진 멤버들이 있을 때") {
            val member1 = createTestMember(ObjectId(), setOf("token1", "token2"))
            val member2 = createTestMember(ObjectId(), setOf("token3"))
            val member3 = createTestMember(ObjectId(), setOf("token4"))

            val groupMember1 = createTestGroupMember(testGroupId, member1.id)
            val groupMember2 = createTestGroupMember(testGroupId, member2.id)
            val groupMember3 = createTestGroupMember(testGroupId, member3.id)

            every { groupMemberRepository.findByGroupId(testGroupId) } returns listOf(
                groupMember1, groupMember2, groupMember3
            )
            every { loadMemberPort.loadByIds(any()) } returns listOf(member1, member2, member3)
            every { pushService.sendPushNotifications(any(), any(), any(), any(), any()) } returns Unit

            then("모든 그룹 멤버들에게 푸시 알림이 전송되어야 한다") {
                pushEventHandler.handleGroupPushEvent(event)
            }
        }

        `when`("그룹 멤버들이 디바이스 토큰을 가지고 있지 않을 때") {
            val member1 = createTestMember(ObjectId(), emptySet())
            val member2 = createTestMember(ObjectId(), emptySet())

            val groupMember1 = createTestGroupMember(testGroupId, member1.id)
            val groupMember2 = createTestGroupMember(testGroupId, member2.id)

            every { groupMemberRepository.findByGroupId(testGroupId) } returns listOf(
                groupMember1, groupMember2
            )
            every { loadMemberPort.loadByIds(any()) } returns listOf(member1, member2)

            then("푸시 알림이 전송되지 않아야 한다") {
                pushEventHandler.handleGroupPushEvent(event)
            }
        }

        `when`("그룹에 멤버가 없을 때") {
            every { groupMemberRepository.findByGroupId(testGroupId) } returns emptyList()

            then("푸시 알림이 전송되지 않아야 한다") {
                pushEventHandler.handleGroupPushEvent(event)
            }
        }
    }

    given("스케줄 푸시 이벤트가 발생한 상황에서") {
        val event = SchedulePushEvent(
            scheduleId = testScheduleId,
            title = "스케줄 알림",
            body = "곧 스터디가 시작됩니다.",
            image = null,
            data = mapOf("type" to "schedule")
        )

        `when`("스케줄에 참여자들이 있고 디바이스 토큰을 가지고 있을 때") {
            val member1 = createTestMember(ObjectId(), setOf("schedule-token1"))
            val member2 = createTestMember(ObjectId(), setOf("schedule-token2", "schedule-token3"))

            val participant1 = createTestParticipant(testScheduleId, member1.id)
            val participant2 = createTestParticipant(testScheduleId, member2.id)

            every { participantRepository.getByScheduleId(testScheduleId) } returns listOf(
                participant1, participant2
            )
            every { loadMemberPort.loadByIds(any()) } returns listOf(member1, member2)
            every { pushService.sendPushNotifications(any(), any(), any(), any(), any()) } returns Unit

            then("모든 스케줄 참여자들에게 푸시 알림이 전송되어야 한다") {
                pushEventHandler.handleSchedulePushEvent(event)
            }
        }

        `when`("스케줄 참여자들이 디바이스 토큰을 가지고 있지 않을 때") {
            val member1 = createTestMember(ObjectId(), emptySet())
            val participant1 = createTestParticipant(testScheduleId, member1.id)

            every { participantRepository.getByScheduleId(testScheduleId) } returns listOf(participant1)
            every { loadMemberPort.loadByIds(any()) } returns listOf(member1)

            then("푸시 알림이 전송되지 않아야 한다") {
                pushEventHandler.handleSchedulePushEvent(event)
            }
        }

        `when`("스케줄에 참여자가 없을 때") {
            every { participantRepository.getByScheduleId(testScheduleId) } returns emptyList()

            then("푸시 알림이 전송되지 않아야 한다") {
                pushEventHandler.handleSchedulePushEvent(event)
            }
        }
    }

    given("개별 멤버 푸시 이벤트가 발생한 상황에서") {
        val event = MemberPushEvent(
            memberId = testMemberId,
            title = "개인 알림",
            body = "새로운 메시지가 도착했습니다.",
            image = "https://example.com/personal.jpg",
            data = mapOf("type" to "personal", "messageId" to "123")
        )

        `when`("멤버가 존재하고 디바이스 토큰을 가지고 있을 때") {
            val member = createTestMember(testMemberId, setOf("personal-token1", "personal-token2"))

            every { loadMemberPort.loadById(testMemberId.toHexString()) } returns member
            every { pushService.sendPushNotifications(any(), any(), any(), any(), any()) } returns Unit

            then("해당 멤버에게 푸시 알림이 전송되어야 한다") {
                pushEventHandler.handleMemberPushEvent(event)
            }
        }

        `when`("멤버가 존재하지만 디바이스 토큰을 가지고 있지 않을 때") {
            val member = createTestMember(testMemberId, emptySet())

            every { loadMemberPort.loadById(testMemberId.toHexString()) } returns member
            every { pushService.sendPushNotifications(any(), any(), any(), any(), any()) } returns Unit

            then("빈 토큰 리스트로 푸시 알림이 전송되어야 한다") {
                pushEventHandler.handleMemberPushEvent(event)
            }
        }

        `when`("멤버가 존재하지 않을 때") {
            every { loadMemberPort.loadById(testMemberId.toHexString()) } returns null

            then("푸시 알림이 전송되지 않아야 한다") {
                pushEventHandler.handleMemberPushEvent(event)
            }
        }
    }
})
