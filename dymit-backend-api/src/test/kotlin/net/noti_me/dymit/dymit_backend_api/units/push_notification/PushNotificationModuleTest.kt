package net.noti_me.dymit.dymit_backend_api.units.push_notification

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastPushable
import net.noti_me.dymit.dymit_backend_api.common.event.GroupPushable
import net.noti_me.dymit.dymit_backend_api.common.event.Pushable
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.push_notification.adapter.`in`.event.PushEventAdapter
import net.noti_me.dymit.dymit_backend_api.push_notification.adapter.`in`.web.AdminPushNotificationController
import net.noti_me.dymit.dymit_backend_api.push_notification.application.SendGroupPushService
import net.noti_me.dymit.dymit_backend_api.push_notification.application.SendPersonalPushService
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.admin.SendAdminPushUseCase
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group.SendGroupPushUseCase
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group.dto.SendGroupPushCommand
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal.SendPersonalPushUseCase
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal.dto.SendPersonalPushCommand
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.web.dto.AdminPushNotificationRequest
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.SendPushNotificationPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.dto.PushDeliveryDto
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.LoadPushMemberPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.dto.PushDeviceTokenDto
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.dto.PushMemberDto
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.study_group.LoadPushGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.study_group.dto.PushGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventScheduleDto
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus

internal class PushNotificationModuleTest : BehaviorSpec() {

    private val memberPort = mockk<LoadPushMemberPort>()
    private val groupMemberPort = mockk<LoadPushGroupMemberPort>()
    private val deliveryPort = mockk<SendPushNotificationPort>()
    private val personalService = SendPersonalPushService(memberPort, deliveryPort)
    private val groupService = SendGroupPushService(groupMemberPort, memberPort, deliveryPort)

    init {
        afterEach { clearAllMocks() }

        given("a personal push command") {
            `when`("the member has active and inactive device tokens") {
                then("it sends only active tokens with the event name in the payload") {
                    val memberId = ObjectId.get()
                    val delivery = slot<PushDeliveryDto>()
                    every { memberPort.loadById(memberId) } returns PushMemberDto(
                        memberId,
                        listOf(PushDeviceTokenDto("active", true), PushDeviceTokenDto("inactive", false))
                    )
                    every { deliveryPort.send(capture(delivery)) } just runs

                    personalService.execute(personalCommand(memberId))

                    delivery.captured.deviceTokens shouldBe listOf("active")
                    delivery.captured.data shouldBe mapOf("resourceId" to "resource-1", "eventName" to "PERSONAL")
                }
            }

            `when`("the member has no active token") {
                then("it completes without attempting delivery") {
                    val memberId = ObjectId.get()
                    every { memberPort.loadById(memberId) } returns PushMemberDto(
                        memberId,
                        listOf(PushDeviceTokenDto("inactive", false))
                    )

                    personalService.execute(personalCommand(memberId))

                    verify(exactly = 0) { deliveryPort.send(any()) }
                }
            }
        }

        given("a group push command") {
            `when`("group members include an excluded member and duplicate active tokens") {
                then("it excludes the member, deduplicates tokens, and preserves payload data") {
                    val groupId = ObjectId.get()
                    val includedId = ObjectId.get()
                    val excludedId = ObjectId.get()
                    val delivery = slot<PushDeliveryDto>()
                    every { groupMemberPort.loadByGroupId(groupId) } returns listOf(
                        PushGroupMemberDto(includedId), PushGroupMemberDto(excludedId)
                    )
                    every { memberPort.loadByIds(listOf(includedId)) } returns listOf(
                        PushMemberDto(includedId, listOf(PushDeviceTokenDto("same", true), PushDeviceTokenDto("off", false))),
                        PushMemberDto(ObjectId.get(), listOf(PushDeviceTokenDto("same", true), PushDeviceTokenDto("other", true)))
                    )
                    every { deliveryPort.send(capture(delivery)) } just runs

                    groupService.execute(groupCommand(groupId, setOf(excludedId)))

                    delivery.captured.deviceTokens shouldBe listOf("same", "other")
                    delivery.captured.data shouldBe mapOf("groupId" to groupId.toHexString(), "eventName" to "GROUP")
                }
            }
        }

        given("the inbound event adapter") {
            val personalUseCase = mockk<SendPersonalPushUseCase>()
            val groupUseCase = mockk<SendGroupPushUseCase>()
            val adapter = PushEventAdapter(personalUseCase, groupUseCase)

            `when`("personal, group, and broadcast events arrive") {
                then("it maps every message to the push-owned command without losing fields") {
                    val memberId = ObjectId.get()
                    val groupId = ObjectId.get()
                    val excludedId = ObjectId.get()
                    val personalCommand = slot<SendPersonalPushCommand>()
                    val groupCommand = slot<SendGroupPushCommand>()
                    val broadcastCommand = slot<SendPersonalPushCommand>()
                    every { personalUseCase.execute(capture(personalCommand)) } just runs
                    every { groupUseCase.execute(capture(groupCommand)) } just runs
                    adapter.handlePersonalPushEvent(mockk<Pushable> {
                        every { toPushMessage() } returns PersonalPushMessage(memberId, "PERSONAL", "title", "body", "image", mapOf("key" to "value"))
                    })
                    adapter.handleGroupPushEvent(mockk<GroupPushable> {
                        every { toGroupPush() } returns GroupPushMessage(groupId, "GROUP", "group", "body", "image", mapOf("key" to "value"), mutableSetOf(excludedId))
                    })
                    every { personalUseCase.execute(capture(broadcastCommand)) } just runs
                    adapter.handleBroadcastPushEvent(mockk<BroadcastPushable> {
                        every { toPushMessages() } returns listOf(PersonalPushMessage(memberId, "BROADCAST", "title", "body", null, mapOf("key" to "value")))
                    })

                    personalCommand.captured.memberId shouldBe memberId
                    personalCommand.captured.eventName shouldBe "PERSONAL"
                    personalCommand.captured.image shouldBe "image"
                    groupCommand.captured.groupId shouldBe groupId
                    groupCommand.captured.excludedMemberIds shouldBe setOf(excludedId)
                    groupCommand.captured.data shouldBe mapOf("key" to "value")
                    broadcastCommand.captured.eventName shouldBe "BROADCAST"
                    broadcastCommand.captured.data shouldBe mapOf("key" to "value")
                }
            }

            `when`("a study-schedule creation event arrives") {
                then("it preserves group, schedule, owner, and event data in a group command") {
                    val groupId = ObjectId.get()
                    val scheduleId = ObjectId.get()
                    val ownerId = ObjectId.get()
                    val command = slot<SendGroupPushCommand>()
                    every { groupUseCase.execute(capture(command)) } just runs

                    adapter.handleStudyScheduleCreated(
                        StudyScheduleCreatedEventDto(
                            group = StudyScheduleEventGroupDto(groupId.toHexString(), ownerId.toHexString(), "Algorithm", "thumbnail"),
                            schedule = StudyScheduleEventScheduleDto(scheduleId.toHexString(), groupId.toHexString(), 3)
                        )
                    )

                    command.captured.groupId shouldBe groupId
                    command.captured.eventName shouldBe "STUDY_SCHEDULE_CREATED"
                    command.captured.title shouldBe "Algorithm"
                    command.captured.data shouldBe mapOf(
                        "groupId" to groupId.toHexString(),
                        "scheduleId" to scheduleId.toHexString(),
                        "ownerId" to ownerId.toHexString()
                    )
                }
            }
        }

        given("an admin push request") {
            `when`("the web adapter receives it") {
                then("it preserves the existing route and maps the request to the admin command") {
                    val useCase = mockk<SendAdminPushUseCase>()
                    val controller = AdminPushNotificationController(useCase)
                    val command = slot<net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.admin.dto.SendAdminPushCommand>()
                    val request = AdminPushNotificationRequest("notice", listOf(ObjectId.get().toHexString()))
                    every { useCase.execute(capture(command)) } just runs

                    controller.sendPushNotifications(MemberInfo.of(ObjectId.get().toHexString(), "admin", listOf("ROLE_ADMIN")), request)

                    command.captured.message shouldBe "notice"
                    command.captured.memberIds shouldBe request.receiverIds
                    val method = AdminPushNotificationController::class.java.methods.single { it.name == "sendPushNotifications" }
                    method.getAnnotation(PostMapping::class.java).value.toList() shouldBe listOf("/api/v1/admin/push-notifications")
                    method.getAnnotation(ResponseStatus::class.java).value shouldBe HttpStatus.CREATED
                }
            }
        }
    }

    private fun personalCommand(memberId: ObjectId): SendPersonalPushCommand {
        return SendPersonalPushCommand(memberId, "PERSONAL", "title", "body", "image", mapOf("resourceId" to "resource-1"))
    }

    private fun groupCommand(groupId: ObjectId, excludedIds: Set<ObjectId>): SendGroupPushCommand {
        return SendGroupPushCommand(groupId, "GROUP", "title", "body", "image", mapOf("groupId" to groupId.toHexString()), excludedIds)
    }
}
