package net.noti_me.dymit.dymit_backend_api.units.admin

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.admin.adapter.`in`.web.AdminPushNotificationController
import net.noti_me.dymit.dymit_backend_api.admin.application.GetDailyMemberStatusService
import net.noti_me.dymit.dymit_backend_api.admin.application.SendAdminPushService
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.AdminPushNotificationRequest
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.GetDailyMemberStatusCommand
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.SendAdminPushCommand
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.member.AdminMemberStatusPort
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.member.dto.AdminMemberStatusDto
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification.AdminPushNotificationPort
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification.dto.AdminPushNotificationDto
import net.noti_me.dymit.dymit_backend_api.admin.application.usecase.SendAdminPushUseCase
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDateTime

internal class AdminModuleTest : BehaviorSpec() {

    private val memberStatusPort = mockk<AdminMemberStatusPort>()
    private val pushPort = mockk<AdminPushNotificationPort>()

    init {
        afterEach { clearAllMocks() }

        given("a KST daily member status query") {
            `when`("the admin use case executes") {
                then("it queries UTC and maps the admin-owned port DTO") {
                    val start = LocalDateTime.of(2026, 7, 26, 9, 0)
                    val end = start.plusDays(1)
                    val status = AdminMemberStatusDto(1, 2, 3, 4, start.minusHours(9))
                    val starts = slot<LocalDateTime>()
                    val ends = slot<LocalDateTime>()
                    every { memberStatusPort.findAllByCreatedAtBetween(capture(starts), capture(ends)) } returns listOf(status)

                    val result = GetDailyMemberStatusService(memberStatusPort).execute(GetDailyMemberStatusCommand(start, end))

                    starts.captured shouldBe start.minusHours(9)
                    ends.captured shouldBe end.minusHours(9)
                    result.single().recordedAt shouldBe status.createdAt
                    result.single().totalMemberCount shouldBe 4
                }
            }
        }

        given("an admin push command") {
            `when`("the use case executes") {
                then("it dispatches one unchanged personal-push payload per receiver") {
                    val first = ObjectId.get().toHexString()
                    val second = ObjectId.get().toHexString()
                    val notifications = mutableListOf<AdminPushNotificationDto>()
                    every { pushPort.send(capture(notifications)) } just runs

                    SendAdminPushService(pushPort).execute(SendAdminPushCommand("notice", listOf(first, second)))

                    notifications.map { it.memberId.toHexString() } shouldBe listOf(first, second)
                    notifications.forEach {
                        it.title shouldBe "Dymit"
                        it.body shouldBe "notice"
                        it.eventName shouldBe "admin_push_notification"
                        it.data shouldBe emptyMap()
                        it.image shouldBe null
                    }
                }
            }
        }

        given("an admin push REST request") {
            `when`("the controller receives it") {
                then("it keeps route, CREATED, LoginMember, and request-to-command behavior") {
                    val useCase = mockk<SendAdminPushUseCase>()
                    val command = slot<SendAdminPushCommand>()
                    every { useCase.execute(capture(command)) } just runs
                    val request = AdminPushNotificationRequest("notice", listOf(ObjectId.get().toHexString()))

                    AdminPushNotificationController(useCase).sendPushNotifications(
                        MemberInfo.of(ObjectId.get().toHexString(), "admin", listOf("ROLE_ADMIN")),
                        request
                    )

                    command.captured shouldBe SendAdminPushCommand(request.message, request.receiverIds)
                    val method = AdminPushNotificationController::class.java.methods.single { it.name == "sendPushNotifications" }
                    method.getAnnotation(PostMapping::class.java).value.toList() shouldBe listOf("/api/v1/admin/push-notifications")
                    method.getAnnotation(ResponseStatus::class.java).value shouldBe HttpStatus.CREATED
                    method.parameters[0].annotations.any { it is LoginMember } shouldBe true
                    verify(exactly = 1) { useCase.execute(any()) }
                }
            }
        }
    }
}
