package net.noti_me.dymit.dymit_backend_api.units.task.adapter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.supports.createStatisticsMongoConverter
import net.noti_me.dymit.dymit_backend_api.supports.scheduleBsonRoundTrip
import net.noti_me.dymit.dymit_backend_api.task.adapter.out.persistence.mongo.MongoManagedTaskQueryAdapter
import net.noti_me.dymit.dymit_backend_api.task.application.ManagedTaskCursor
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationResults
import java.time.Instant
import java.time.LocalDate
import java.util.Date

internal class MongoManagedTaskQueryAdapterTest : BehaviorSpec({
    val start = Instant.parse("2026-10-10T15:00:00Z")
    val end = start.plusSeconds(86400)
    Given("소유 그룹 과제 조회의 실제 BSON projection") {
        Then("부분 문서는 엔티티 생성 없이 ObjectId와 Date를 응답 DTO로 변환한다") {
            val template = mockk<MongoTemplate>()
            val id = ObjectId()
            val group = ObjectId()
            val raw = scheduleBsonRoundTrip(Document("_id", id).append("title", "등록된 과제")
                .append("expireAt", Date.from(start)).append("groupId", group).append("groupName", "현재 이름"))
            val document = createStatisticsMongoConverter().read(Document::class.java, raw)
            every { template.aggregate(any<Aggregation>(), "tasks", Document::class.java) } returns AggregationResults(listOf(document), Document())
            val result = MongoManagedTaskQueryAdapter(template).load(ObjectId(), start, end, null, 21).single()
            result.taskId shouldBe id
            result.groupId shouldBe group
            result.groupName shouldBe "현재 이름"
            result.title shouldBe "등록된 과제"
            result.expireAt shouldBe start
            verify(exactly = 1) { template.aggregate(any<Aggregation>(), "tasks", Document::class.java) }
        }
    }
    listOf(false, true).forEach { hasCursor ->
        Given("소유권과 유효 관계가 필요한 집계 커서=$hasCursor") {
            Then("날짜 및 복합 위치를 제한하고 소유권 필터 이후 DB limit을 적용한다") {
                val template = mockk<MongoTemplate>()
                val captured = slot<Aggregation>()
                every { template.aggregate(capture(captured), "tasks", Document::class.java) } returns AggregationResults(emptyList<Document>(), Document())
                val owner = ObjectId()
                val cursor = if (hasCursor) ManagedTaskCursor(LocalDate.of(2026,10,11), start.plusMillis(1000), ObjectId()) else null
                MongoManagedTaskQueryAdapter(template).load(owner, start, end, cursor, 21)
                val pipeline = captured.captured.toPipeline(Aggregation.DEFAULT_CONTEXT)
                val matches = pipeline.filter { it.containsKey("\$match") }.map { it.get("\$match", Document::class.java) }
                val taskFilter = if (cursor == null) matches[0] else (matches[0]["\$and"] as List<*>)[0] as Document
                taskFilter["expireAt"] shouldBe Document("\$gte", start).append("\$lt", end)
                taskFilter["isDeleted"] shouldBe Document("\$ne", true)
                matches[1] shouldBe Document("schedule.isDeleted", Document("\$ne", true))
                matches[2] shouldBe Document("group.ownerId", owner).append("group.isDeleted", Document("\$ne", true))
                val ownerIndex = pipeline.indexOfFirst { it["\$match"] == matches[2] }
                val limitIndex = pipeline.indexOfFirst { it.containsKey("\$limit") }
                (ownerIndex < limitIndex) shouldBe true
                pipeline[limitIndex]["\$limit"] shouldBe 21L
                pipeline.single { it.containsKey("\$sort") }["\$sort"] shouldBe Document("expireAt", 1).append("_id", 1)
                pipeline.filter { it.containsKey("\$unwind") }.map { it["\$unwind"] } shouldBe listOf("\$schedule", "\$group")
                pipeline.filter { it.containsKey("\$lookup") }.map { (it["\$lookup"] as Document)["from"] } shouldBe listOf("study_schedules", "study_groups")
                if (cursor != null) {
                    val predicates = (matches[0]["\$and"] as List<*>)[1] as Document
                    predicates shouldBe Document("\$or", listOf(Document("expireAt", Document("\$gt", cursor.expireAt)),
                        Document("\$and", listOf(Document("expireAt", cursor.expireAt), Document("_id", Document("\$gt", cursor.taskId))))))
                }
                // 제출 문서와 대상자 존재 여부가 과제 목록을 제한해서는 안 된다.
                pipeline.toString().contains("task_submissions") shouldBe false
                pipeline.toString().contains("task_assignees") shouldBe false
                pipeline.toString().contains("memberId") shouldBe false
                verify(exactly = 1) { template.aggregate(any<Aggregation>(), "tasks", Document::class.java) }
            }
        }
    }
    Given("여러 과제의 현재 미제출 대상자 수") {
        Then("NOT_SUBMITTED와 삭제 상태만 사용해 일괄 집계하고 BSON 숫자 형식을 수용한다") {
            val template = mockk<MongoTemplate>()
            val captured = slot<Aggregation>()
            val ids = List(2) { ObjectId() }
            val docs = listOf(Document("_id",ids[0]).append("count",2), Document("_id",ids[1]).append("count",3L)).map(::scheduleBsonRoundTrip)
            every { template.aggregate(capture(captured), "task_assignees", Document::class.java) } returns AggregationResults(docs, Document())
            MongoManagedTaskQueryAdapter(template).countNotSubmittedAssignees(ids) shouldBe mapOf(ids[0] to 2L, ids[1] to 3L)
            val match = captured.captured.toPipeline(Aggregation.DEFAULT_CONTEXT)[0]["\$match"] as Document
            match.keys shouldBe setOf("taskId", "status", "isDeleted")
            match["taskId"] shouldBe Document("\$in", ids)
            match["status"].toString() shouldBe "NOT_SUBMITTED"
            match["isDeleted"] shouldBe Document("\$ne", true)
            verify(exactly = 1) { template.aggregate(any<Aggregation>(), "task_assignees", Document::class.java) }
        }
    }
    Given("집계할 과제가 없는 목록") {
        Then("추가 DB 호출을 생략한다") {
            val template = mockk<MongoTemplate>()
            MongoManagedTaskQueryAdapter(template).countNotSubmittedAssignees(emptyList()) shouldBe emptyMap()
            verify { template wasNot Called }
        }
    }
})
