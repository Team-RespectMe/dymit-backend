package net.noti_me.dymit.dymit_backend_api.units.study_schedule.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.statistics.MongoStudyScheduleStatisticsQueryAdapter
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import net.noti_me.dymit.dymit_backend_api.supports.createStatisticsMongoConverter
import net.noti_me.dymit.dymit_backend_api.supports.scheduleBsonRoundTrip
import net.noti_me.dymit.dymit_backend_api.supports.scheduleReferenceProjection
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mapping.model.MappingInstantiationException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant
import java.util.Date

internal class ScheduleReferenceProjectionTest : BehaviorSpec({

    Given("일정 참조의 세 필드만 실제 BSON에 남긴 부분 문서") {
        Then("기존 전체 엔티티 매핑은 scheduleAt 생성자 값 누락으로 실패하고 Document 매핑은 원본 값을 보존한다") {
            val converter = createStatisticsMongoConverter()
            val id = ObjectId()
            val starts = Instant.parse("2026-12-01T12:00:00.123Z")
            val updated = Instant.parse("2026-10-10T10:30:00.456Z")
            val complete = scheduleBsonRoundTrip(Document("_id", id)
                .append("groupId", ObjectId()).append("scheduleAt", Date.from(starts))
                .append("deletedAt", null).append("updatedAt", Date.from(updated)))
            val mapped = converter.read(StudySchedule::class.java, complete)
            mapped.id shouldBe id
            mapped.scheduleAt shouldBe starts
            val projected = scheduleReferenceProjection(complete)
            projected.keys shouldBe setOf("_id", "deletedAt", "updatedAt")
            val failure = shouldThrow<MappingInstantiationException> {
                converter.read(StudySchedule::class.java, projected)
            }
            generateSequence<Throwable>(failure) { it.cause }.joinToString { it.message.orEmpty() } shouldContain "scheduleAt"
            val document = converter.read(Document::class.java, projected)
            document.getObjectId("_id") shouldBe id
            document.getDate("updatedAt").toInstant() shouldBe updated
            document["deletedAt"] shouldBe null
        }
    }

    Given("삭제된 일정과 미래 일정에서 투영된 실제 BSON 문서") {
        Then("명시된 컬렉션의 Document만 조회하고 원본 ID와 BSON 날짜를 반환하며 추가 필터를 적용하지 않는다") {
            val mongo = mockk<MongoTemplate>()
            val group = ObjectId()
            val deletedId = ObjectId()
            val futureId = ObjectId()
            val deletedAt = Instant.parse("2026-10-10T09:00:00.123Z")
            val updatedAt = Instant.parse("2026-10-11T10:00:00.456Z")
            val documents = listOf(
                scheduleReferenceProjection(Document("_id", deletedId).append("groupId", group)
                    .append("scheduleAt", Date.from(deletedAt.minusSeconds(60))).append("isDeleted", true)
                    .append("deletedAt", Date.from(deletedAt)).append("updatedAt", Date.from(updatedAt))),
                scheduleReferenceProjection(Document("_id", futureId).append("groupId", group)
                    .append("scheduleAt", Date.from(Instant.parse("2026-12-01T00:00:00Z"))).append("isDeleted", false)
                    .append("deletedAt", null).append("updatedAt", Date.from(updatedAt)))
            )
            val query = slot<Query>()
            every { mongo.find(capture(query), Document::class.java, "study_schedules") } returns documents
            val references = MongoStudyScheduleStatisticsQueryAdapter(mongo).loadScheduleReferences(group)
            references.map { it.scheduleId } shouldBe listOf(deletedId, futureId)
            references.map { it.deletedAt } shouldBe listOf(deletedAt, null)
            references.map { it.updatedAt } shouldBe listOf(updatedAt, updatedAt)
            query.captured.queryObject shouldBe Document("groupId", group)
            query.captured.fieldsObject shouldBe Document("_id", 1).append("deletedAt", 1).append("updatedAt", 1)
            verify(exactly = 1) { mongo.find(any<Query>(), Document::class.java, "study_schedules") }
            verify(exactly = 0) { mongo.find(any<Query>(), StudySchedule::class.java) }
        }
    }

    Given("nullable 시각이 없거나 명시적으로 null인 기존 일정 참조") {
        Then("필드가 없는 경우에도 ID를 보존하고 두 시각을 null로 반환한다") {
            val mongo = mockk<MongoTemplate>()
            val ids = listOf(ObjectId(), ObjectId())
            val documents = listOf(scheduleBsonRoundTrip(Document("_id", ids[0])),
                scheduleBsonRoundTrip(Document("_id", ids[1]).append("deletedAt", null).append("updatedAt", null)))
            every { mongo.find(any<Query>(), Document::class.java, "study_schedules") } returns documents
            val references = MongoStudyScheduleStatisticsQueryAdapter(mongo).loadScheduleReferences(ObjectId())
            references.map { it.scheduleId } shouldBe ids
            references.map { it.deletedAt } shouldBe listOf(null, null)
            references.map { it.updatedAt } shouldBe listOf(null, null)
        }
    }

    Given("일정 참조가 없는 그룹") {
        Then("일정 엔티티를 생성하지 않고 빈 참조 목록을 반환한다") {
            val mongo = mockk<MongoTemplate>()
            every { mongo.find(any<Query>(), Document::class.java, "study_schedules") } returns emptyList()
            MongoStudyScheduleStatisticsQueryAdapter(mongo).loadScheduleReferences(ObjectId()) shouldBe emptyList()
            verify(exactly = 0) { mongo.find(any<Query>(), StudySchedule::class.java) }
        }
    }
})
