package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.task.application.TaskScheduleSyncEventHandler
import net.noti_me.dymit.dymit_backend_api.task.application.TaskService
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.SyncedTaskDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto as StudySchedule
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventMemberDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventScheduleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipatedEventDto
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskCreatedBroadcastEvent
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant

internal class TaskScheduleSyncEventHandlerTest : BehaviorSpec() {

    private val taskService = mockk<TaskService>(relaxed = true)
    private val support = mockk<TaskServiceSupport>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val handler = TaskScheduleSyncEventHandler(taskService, support, eventPublisher)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("일정 참여 이벤트 핸들러") {
            When("동기화 결과로 신규 assignee가 반영된 사전 과제가 여러 개 반환되면") {
                Then("반환된 과제들에 대해서만 신규 참여자 1명에게 과제 생성 브로드캐스트를 과제별로 발행한다") {
                    val group = createGroup("스터디")
                    val schedule = createSchedule(group.id!!)
                    val member = createMember(group.id!!, "새 멤버")
                    val task1 = createTask(schedule.id!!, "사전 과제 1")
                    val task2 = createTask(schedule.id!!, "사전 과제 2")
                    val event = createParticipatedEvent(group, schedule, member)
                    val publishedEvents = mutableListOf<TaskCreatedBroadcastEvent>()

                    every { support.loadGroup(group.identifier) } returns group
                    every {
                        taskService.syncParticipatedScheduleTasks(schedule.id.toHexString(), member.memberId)
                    } returns listOf(SyncedTaskDto(task1.identifier), SyncedTaskDto(task2.identifier))
                    every { eventPublisher.publishEvent(capture(publishedEvents)) } returns Unit

                    handler.onScheduleParticipated(event)

                    verify(exactly = 1) {
                        taskService.syncParticipatedScheduleTasks(schedule.id.toHexString(), member.memberId)
                    }
                    verify(exactly = 2) { eventPublisher.publishEvent(any<TaskCreatedBroadcastEvent>()) }
                    publishedEvents.map { it.toPersonalPushMessages().single().memberId } shouldContainExactly listOf(
                        ObjectId(member.memberId),
                        ObjectId(member.memberId)
                    )
                    publishedEvents.map { it.toPersonalPushMessages().single().data["taskId"] } shouldContainExactly listOf(
                        task1.identifier,
                        task2.identifier
                    )
                    publishedEvents.forEach { published ->
                        published.toPersonalFeedData().single().eventName shouldBe "TASK_CREATED"
                    }
                }
            }

            When("동기화 결과가 비어 있으면") {
                Then("브로드캐스트는 발행하지 않는다") {
                    val group = createGroup("스터디")
                    val schedule = createSchedule(group.id!!)
                    val member = createMember(group.id!!, "새 멤버")
                    val event = createParticipatedEvent(group, schedule, member)

                    every {
                        taskService.syncParticipatedScheduleTasks(schedule.id.toHexString(), member.memberId)
                    } returns emptyList()

                    handler.onScheduleParticipated(event)

                    verify(exactly = 1) {
                        taskService.syncParticipatedScheduleTasks(schedule.id.toHexString(), member.memberId)
                    }
                    verify(exactly = 0) { eventPublisher.publishEvent(any<TaskCreatedBroadcastEvent>()) }
                }
            }
        }
    }

    private fun createGroup(name: String): StudyGroup {
        return StudyGroup(
            id = ObjectId.get(),
            ownerId = ObjectId.get(),
            name = name,
            description = "설명"
        )
    }

    private fun createSchedule(groupId: ObjectId): StudySchedule {
        return StudySchedule(
            id = ObjectId.get(),
            groupId = groupId,
            title = "일정",
            session = 1L,
            scheduleAt = Instant.now().plusSeconds(1L * 86400L)
        )
    }

    private fun createMember(groupId: ObjectId, nickname: String): StudyScheduleEventMemberDto {
        return StudyScheduleEventMemberDto(
            memberId = ObjectId.get().toHexString(),
            nickname = nickname
        )
    }

    private fun createParticipatedEvent(
        group: StudyGroup,
        schedule: StudySchedule,
        member: StudyScheduleEventMemberDto
    ): StudyScheduleParticipatedEventDto {
        return StudyScheduleParticipatedEventDto(
            group = StudyScheduleEventGroupDto(
                id = group.identifier,
                ownerId = group.ownerId.toHexString(),
                name = group.name,
                profileImageThumbnail = group.profileImage.thumbnail
            ),
            schedule = StudyScheduleEventScheduleDto(
                id = schedule.id.toHexString(),
                groupId = schedule.groupId.toHexString(),
                session = schedule.session
            ),
            member = member
        )
    }

    private fun createTask(scheduleId: ObjectId, title: String): Task {
        return Task(
            id = ObjectId.get(),
            relatedScheduleId = scheduleId,
            type = TaskType.PRE,
            title = title,
            description = "설명",
            attachments = emptyList(),
            expireAt = Instant.now().plusSeconds(2L * 86400L)
        )
    }
}
