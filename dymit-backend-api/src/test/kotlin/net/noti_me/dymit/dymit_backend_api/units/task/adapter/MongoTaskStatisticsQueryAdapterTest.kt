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
    Given("과거 상태 이력이 있지만 이번 주에 새로 제출한 과제") {
        Then("과거 기준 상태만으로 불필요한 전체 이력 재계산을 요청하지 않는다") {
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
            result.requiresReplay shouldBe false
            result.sources.single().assignments.single().statusHistory.size shouldBe 2
            assignmentQueries.clear()
            adapter.loadChanged(
                TaskStatisticsQuery(groupId, memberId, previous.minusSeconds(604800), previous,
                    previous.plusSeconds(604800), observed.plusSeconds(60), previous.plusSeconds(604801))
            )
            val mutationQuery = assignmentQueries.single { it.queryObject.containsKey("updatedAt") }
            val timestampBounds = mutationQuery.queryObject["updatedAt"] as Document
            // 같은 주 조회로 관측 커서가 전진해도 업무 상한부터 변경을 다시 확인합니다.
            timestampBounds["\$gte"] shouldBe previous
        }
    }
})
