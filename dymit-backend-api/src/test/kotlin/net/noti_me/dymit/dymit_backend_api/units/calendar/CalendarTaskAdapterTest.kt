package net.noti_me.dymit.dymit_backend_api.units.calendar

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.calendar.adapter.out.task.MongoCalendarTaskAdapter
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import net.noti_me.dymit.dymit_backend_api.task.domain.*
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant

internal class CalendarTaskAdapterTest : BehaviorSpec() {

    private val mongo = mockk<MongoTemplate>()
    private val adapter = MongoCalendarTaskAdapter(mongo)
    private val memberId = ObjectId.get()
    private val groupId = ObjectId.get()
    private val start = Instant.parse("2026-10-01T15:00:00Z")
    private val end = start.plusSeconds(86400)

    init {
        afterEach { clearAllMocks() }
        Given("본인에게 제출 완료 상태로 배정된 모든 과제 유형") {
            Then("일정 참가 여부와 일정 주차를 요구하지 않고 과제 마감일로 조회한다") {
                val scheduleId = ObjectId.get()
                val tasks = TaskType.values().flatMap { type ->
                    TaskSubmissionType.values().map { submissionType ->
                        Task(scheduleId, type, "$type $submissionType", "설명", emptyList(), start,
                            id = ObjectId.get(), submissionType = submissionType)
                    }
                }
                val assigneeQuery = slot<Query>()
                val taskQuery = slot<Query>()
                val scheduleQuery = slot<Query>()
                every { mongo.find(capture(assigneeQuery), TaskAssignee::class.java) } returns tasks.map {
                    TaskAssignee(taskId = it.id!!, memberId = memberId, status = TaskAssigneeStatus.SUBMITTED)
                }
                every { mongo.find(capture(taskQuery), Task::class.java) } returns tasks
                every { mongo.find(capture(scheduleQuery), StudySchedule::class.java) } returns listOf(
                    StudySchedule(id = scheduleId, groupId = groupId, title = "지난 주 일정", scheduleAt = start.minusSeconds(86400 * 14))
                )
                val result = adapter.loadActiveTasks(memberId.toHexString(), listOf(groupId.toHexString()), start, end)
                result.size shouldBe 4
                result.map { it.id } shouldBe tasks.map { it.identifier }
                result.all { it.groupId == groupId.toHexString() && it.eventAt == start } shouldBe true
                result.map { it.title } shouldBe tasks.map { it.title }
                assigneeQuery.captured.queryObject shouldBe org.bson.Document(mapOf("memberId" to memberId, "isDeleted" to false))
                taskQuery.captured.queryObject["_id"] shouldBe mapOf("${'$'}in" to tasks.map { it.id })
                taskQuery.captured.queryObject["expireAt"] shouldBe mapOf("${'$'}gte" to start, "${'$'}lt" to end)
                taskQuery.captured.queryObject["isDeleted"] shouldBe false
                taskQuery.captured.queryObject.keys shouldBe setOf("_id", "expireAt", "isDeleted")
                taskQuery.captured.sortObject.isEmpty() shouldBe true
                assigneeQuery.captured.sortObject.isEmpty() shouldBe true
                scheduleQuery.captured.sortObject.isEmpty() shouldBe true
                scheduleQuery.captured.queryObject shouldBe org.bson.Document(mapOf(
                    "_id" to mapOf("${'$'}in" to listOf(scheduleId)),
                    "groupId" to mapOf("${'$'}in" to listOf(groupId)), "isDeleted" to false
                ))
                listOf(assigneeQuery, taskQuery, scheduleQuery).forEach { it.captured.limit shouldBe 0 }
                verify(exactly = 1) { mongo.find(any(), TaskAssignee::class.java) }
                verify(exactly = 1) { mongo.find(any(), Task::class.java) }
                verify(exactly = 1) { mongo.find(any(), StudySchedule::class.java) }
                verify(exactly = 0) { mongo.find(any(), ScheduleParticipant::class.java) }
            }
        }
        listOf("다른 회원에게만 배정", "본인 제출 대상 삭제").forEach { situation ->
            Given("$situation 상태라 활성 본인 관계가 없으면") {
                Then("과제와 관련 일정을 조회하지 않는다") {
                    val captured = slot<Query>()
                    every { mongo.find(capture(captured), TaskAssignee::class.java) } returns emptyList()
                    adapter.loadActiveTasks(memberId.toHexString(), listOf(groupId.toHexString()), start, end) shouldBe emptyList()
                    captured.captured.queryObject shouldBe org.bson.Document(mapOf("memberId" to memberId, "isDeleted" to false))
                    verify(exactly = 0) { mongo.find(any(), Task::class.java) }
                    verify(exactly = 0) { mongo.find(any(), StudySchedule::class.java) }
                }
            }
        }
        Given("과제가 삭제되거나 마감일이 기간 밖이면") {
            Then("활성 기간 과제 결과가 없어 관련 일정 조회를 생략한다") {
                every { mongo.find(any<Query>(), TaskAssignee::class.java) } returns listOf(TaskAssignee(ObjectId.get(), memberId))
                every { mongo.find(any<Query>(), Task::class.java) } returns emptyList()
                adapter.loadActiveTasks(memberId.toHexString(), listOf(groupId.toHexString()), start, end) shouldBe emptyList()
                verify(exactly = 0) { mongo.find(any(), StudySchedule::class.java) }
            }
        }
        listOf("누락", "삭제", "탈퇴 그룹 소속").forEach { situation ->
            Given("과제의 관련 일정이 $situation 상태이면") {
                Then("현재 활성 그룹의 관련 일정이 없는 과제를 제외한다") {
                    val task = Task(ObjectId.get(), TaskType.PRE, "과제", "", emptyList(), start, id = ObjectId.get())
                    every { mongo.find(any<Query>(), TaskAssignee::class.java) } returns listOf(TaskAssignee(task.id!!, memberId))
                    every { mongo.find(any<Query>(), Task::class.java) } returns listOf(task)
                    val captured = slot<Query>()
                    every { mongo.find(capture(captured), StudySchedule::class.java) } returns emptyList()
                    adapter.loadActiveTasks(memberId.toHexString(), listOf(groupId.toHexString()), start, end) shouldBe emptyList()
                    captured.captured.queryObject["groupId"] shouldBe mapOf("${'$'}in" to listOf(groupId))
                    captured.captured.queryObject["isDeleted"] shouldBe false
                }
            }
        }
        Given("활성 소속 그룹이 없으면") {
            Then("과제 제출 대상까지 조회하지 않는다") {
                adapter.loadActiveTasks(memberId.toHexString(), emptyList(), start, end) shouldBe emptyList()
                verify { mongo wasNot Called }
            }
        }
    }
}
