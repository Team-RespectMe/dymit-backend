package net.noti_me.dymit.dymit_backend_api.units.feed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResource
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.GroupFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.GroupFeedEventData
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.feed.adapter.`in`.event.CommonFeedEventAdapter
import net.noti_me.dymit.dymit_backend_api.feed.adapter.`in`.web.UserFeedController
import net.noti_me.dymit.dymit_backend_api.feed.application.DeleteUserFeedService
import net.noti_me.dymit.dymit_backend_api.feed.application.MarkUserFeedAsReadService
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreateGroupFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreatePersonalFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreateGroupFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreatePersonalFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.DeleteUserFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.GetUserFeedsUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.MarkUserFeedAsReadUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.DeleteUserFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.MarkUserFeedAsReadCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.FeedMessage
import net.noti_me.dymit.dymit_backend_api.feed.domain.IconType
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeed
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

internal class FeedModuleTest : BehaviorSpec() {

    private val persistencePort = mockk<UserFeedPersistencePort>()
    private val deleteService = DeleteUserFeedService(persistencePort)
    private val markAsReadService = MarkUserFeedAsReadService(persistencePort)

    init {
        afterEach { clearAllMocks() }

        given("a user feed owned by another member") {
            `when`("delete or read is requested") {
                then("both operations reject the request without changing persistence") {
                    val feed = userFeed(ObjectId.get())
                    every { persistencePort.findById(feed.identifier) } returns feed

                    shouldThrow<ForbiddenException> {
                        deleteService.execute(DeleteUserFeedCommand(ObjectId.get().toHexString(), feed.identifier))
                    }
                    shouldThrow<ForbiddenException> {
                        markAsReadService.execute(MarkUserFeedAsReadCommand(ObjectId.get().toHexString(), feed.identifier))
                    }

                    verify(exactly = 0) { persistencePort.deleteById(any()) }
                    verify(exactly = 0) { persistencePort.save(any()) }
                }
            }
        }

        given("common feed events") {
            val personalUseCase = mockk<CreatePersonalFeedUseCase>()
            val groupUseCase = mockk<CreateGroupFeedUseCase>()
            val adapter = CommonFeedEventAdapter(personalUseCase, groupUseCase)

            `when`("a personal event arrives") {
                then("it creates one Feed command per recipient with preserved metadata") {
                    val command = slot<CreatePersonalFeedCommand>()
                    every { personalUseCase.execute(capture(command)) } just runs
                    adapter.handlePersonalFeedEvent(mockk<PersonalFeedEvent> {
                        every { toPersonalFeedData() } returns listOf(
                            PersonalFeedEventData(
                                memberId = "member-1",
                                iconType = FeedEventIconType.NOTICE,
                                eventName = "TASK_CREATED",
                                messages = listOf(FeedEventMessage("message", "#111111", "#eeeeee")),
                                resources = listOf(FeedEventResource(FeedEventResourceType.TASK, "task-1"))
                            )
                        )
                    })

                    command.captured.memberId shouldBe "member-1"
                    command.captured.iconType shouldBe IconType.NOTICE
                    command.captured.eventName shouldBe "TASK_CREATED"
                    command.captured.messages.single() shouldBe FeedMessage("message", "#111111", "#eeeeee")
                    command.captured.associates.single().resourceId shouldBe "task-1"
                }
            }

            `when`("a group event arrives") {
                then("it preserves the group recipient and exclusions") {
                    val command = slot<CreateGroupFeedCommand>()
                    every { groupUseCase.execute(capture(command)) } just runs
                    adapter.handleGroupFeedEvent(mockk<GroupFeedEvent> {
                        every { toGroupFeedData() } returns GroupFeedEventData(
                            groupId = "group-1",
                            iconType = FeedEventIconType.DATE,
                            eventName = "SCHEDULE_CREATED",
                            title = "Study",
                            messages = listOf(FeedEventMessage("created")),
                            resources = listOf(FeedEventResource(FeedEventResourceType.STUDY_GROUP, "group-1")),
                            excludedMemberIds = setOf("member-1")
                        )
                    })

                    command.captured.groupId shouldBe "group-1"
                    command.captured.eventName shouldBe "SCHEDULE_CREATED"
                    command.captured.excludedMemberIds shouldBe setOf("member-1")
                    command.captured.associates.single().resourceId shouldBe "group-1"
                }
            }
        }

        given("the user feed web adapter") {
            `when`("a delete request arrives") {
                then("it keeps the route, status, and LoginMember-to-command boundary") {
                    val getUseCase = mockk<GetUserFeedsUseCase>()
                    val deleteUseCase = mockk<DeleteUserFeedUseCase>()
                    val readUseCase = mockk<MarkUserFeedAsReadUseCase>()
                    val controller = UserFeedController(getUseCase, deleteUseCase, readUseCase)
                    val command = slot<DeleteUserFeedCommand>()
                    every { deleteUseCase.execute(capture(command)) } just runs

                    controller.deleteUserFeed(MemberInfo.of("member-1", "member", emptyList()), "feed-1")

                    command.captured shouldBe DeleteUserFeedCommand("member-1", "feed-1")
                    UserFeedController::class.java.getAnnotation(RequestMapping::class.java).value.toList() shouldBe listOf("/api/v1/user-feeds")
                    val method = UserFeedController::class.java.methods.single { it.name == "deleteUserFeed" }
                    method.getAnnotation(DeleteMapping::class.java).value.toList() shouldBe listOf("/{feedId}")
                    method.getAnnotation(ResponseStatus::class.java).value shouldBe HttpStatus.NO_CONTENT
                }
            }
        }
    }

    private fun userFeed(memberId: ObjectId): UserFeed {
        return UserFeed(
            id = ObjectId.get(),
            memberId = memberId,
            iconType = IconType.NOTICE,
            eventName = "EVENT",
            messages = listOf(FeedMessage("message")),
            associates = emptyList()
        )
    }
}
