package net.noti_me.dymit.dymit_backend_api.units.task.adapter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleStatisticsQueryPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsReferenceDto
import net.noti_me.dymit.dymit_backend_api.task.adapter.out.statistics.MongoTaskStatisticsQueryAdapter
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto.TaskStatisticsQuery
import net.noti_me.dymit.dymit_backend_api.task.domain.*
import org.bson.types.ObjectId
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant

internal class MongoTaskStatisticsQueryAdapterTest : BehaviorSpec({
    Given("과거 상태 이력이 있지만 이전 회차 이후에 새로 제출한 과제") {
        Then("기존 과제 변경을 재투영 대상으로 반환하고 관측 커서와 업무 상한을 유지한다") {
            val mongo = mockk<MongoTemplate>()
            val schedules = mockk<StudyScheduleStatisticsQueryPort>()
            val previous = Instant.parse("2026-09-20T15:00:00Z")
            val observed = previous.plusSeconds(60)
            val cutoff = previous
            val scheduleId = ObjectId()
            val memberId = ObjectId()
            val groupId = ObjectId()
            val task = Task(
                relatedScheduleId = scheduleId, type = TaskType.PRE, title = "과제", description = "",
                attachments = emptyList(), expireAt = previous.minusSeconds(1), id = ObjectId(),
                createdAt = previous.minusSeconds(604800), updatedAt = previous.minusSeconds(604800)
            )
            val assignee = TaskAssignee(
                taskId = task.id!!, memberId = memberId, status = TaskAssigneeStatus.SUBMITTED,
                createdAt = previous.minusSeconds(604800), updatedAt = observed,
                statusHistory = listOf(
                    TaskAssigneeStatusChange(TaskAssigneeStatus.NOT_SUBMITTED, previous.minusSeconds(604800)),
                    TaskAssigneeStatusChange(TaskAssigneeStatus.SUBMITTED, observed)
                )
            )
            every { schedules.loadScheduleReferences(groupId) } returns listOf(
                StudyScheduleStatisticsReferenceDto(scheduleId, null, null)
            )
            every { mongo.find(any<Query>(), Task::class.java) } answers {
                if (firstArg<Query>().queryObject.containsKey("_id")) listOf(task) else emptyList()
            }
            val assignmentQueries = mutableListOf<Query>()
            every { mongo.find(any<Query>(), TaskAssignee::class.java) } answers {
                assignmentQueries.add(firstArg())
                listOf(assignee)
            }
            val adapter = MongoTaskStatisticsQueryAdapter(mongo, schedules)
            val result = adapter.loadChanged(
                TaskStatisticsQuery(groupId, memberId, previous.minusSeconds(604800), previous, cutoff, previous, observed)
            )
            result.requiresReplay shouldBe true
            result.sources.single().assignments.single().statusHistory.size shouldBe 2
            assignmentQueries.clear()
            adapter.loadChanged(
                TaskStatisticsQuery(groupId, memberId, previous.minusSeconds(604800), previous,
                    previous.plusSeconds(604800), observed.plusSeconds(60), previous.plusSeconds(604801))
            )
            val mutationQuery = assignmentQueries.single { it.queryObject.containsKey("updatedAt") }
            val timestampBounds = mutationQuery.queryObject["updatedAt"] as Document
            // 같은 회차 조회로 관측 커서가 전진해도 업무 상한부터 변경을 다시 확인합니다.
            timestampBounds["\$gte"] shouldBe previous
        }
    }

    Given("두 실제 회차 시작 사이에 마감되는 과제 조회") {
        Then("이전 시각은 제외하고 현재 시작 시각은 포함하며 수정 시각과 독립적으로 조회한다") {
            val mongo = mockk<MongoTemplate>()
            val schedules = mockk<StudyScheduleStatisticsQueryPort>()
            val previous = Instant.parse("2026-09-20T10:00:00Z")
            val cutoff = Instant.parse("2026-09-22T13:30:00Z")
            val group = ObjectId()
            val schedule = ObjectId()
            every { schedules.loadScheduleReferences(group) } returns listOf(
                StudyScheduleStatisticsReferenceDto(schedule, null, null)
            )
            val queries = mutableListOf<Query>()
            every { mongo.find(any<Query>(), Task::class.java) } answers {
                queries.add(firstArg())
                emptyList()
            }
            every { mongo.find(any<Query>(), TaskAssignee::class.java) } returns emptyList()
            MongoTaskStatisticsQueryAdapter(mongo, schedules).loadChanged(
                TaskStatisticsQuery(group, ObjectId(), previous.minusSeconds(60), previous, cutoff, previous, cutoff)
            )
            val matured = queries.single { it.queryObject.containsKey("expireAt") }.queryObject
            matured.containsKey("updatedAt") shouldBe false
            (matured["expireAt"] as Document)["\$gt"] shouldBe previous
            (matured["expireAt"] as Document)["\$lte"] shouldBe cutoff
        }
    }
})
