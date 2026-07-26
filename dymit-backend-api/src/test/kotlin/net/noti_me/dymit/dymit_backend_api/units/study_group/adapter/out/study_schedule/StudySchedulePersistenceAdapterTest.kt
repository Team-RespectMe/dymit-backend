package net.noti_me.dymit.dymit_backend_api.units.study_group.adapter.out.study_schedule

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.study_schedule.StudySchedulePersistenceAdapter
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime
import java.util.Date

internal class StudySchedulePersistenceAdapterTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val adapter = StudySchedulePersistenceAdapter(mongoTemplate)

    init {
        given("an empty group ID list") {
            `when`("upcoming schedules are loaded") {
                then("it returns an empty map without querying MongoDB") {
                    adapter.loadUpcomingByGroupIds(emptyList()) shouldBe emptyMap()
                    verify(exactly = 0) { mongoTemplate.find(any<Query>(), any<Class<Document>>(), any<String>()) }
                }
            }
        }

        given("upcoming schedule documents for multiple groups") {
            `when`("the adapter loads schedules") {
                then("it queries future schedules in ascending order and maps the first valid schedule per group") {
                    val firstGroupId = ObjectId.get()
                    val secondGroupId = ObjectId.get()
                    val firstScheduleId = ObjectId.get()
                    val secondScheduleId = ObjectId.get()
                    val scheduleAt = LocalDateTime.of(2026, 7, 27, 10, 0)
                    val query = slot<Query>()
                    every {
                        mongoTemplate.find(capture(query), Document::class.java, "study_schedules")
                    } returns listOf(
                        Document("_id", firstScheduleId)
                            .append("groupId", firstGroupId)
                            .append("title", "First")
                            .append("session", 2)
                            .append("scheduleAt", scheduleAt),
                        Document("_id", ObjectId.get())
                            .append("groupId", firstGroupId)
                            .append("title", "Later")
                            .append("scheduleAt", scheduleAt.plusHours(1)),
                        Document("_id", secondScheduleId)
                            .append("groupId", secondGroupId)
                            .append("title", "Second")
                            .append("scheduleAt", scheduleAt)
                    )

                    val result = adapter.loadUpcomingByGroupIds(
                        listOf(firstGroupId.toHexString(), secondGroupId.toHexString())
                    )

                    verify(exactly = 1) { mongoTemplate.find(any(), Document::class.java, "study_schedules") }
                    (query.captured.queryObject["groupId"] as Document)["\$in"] shouldBe listOf(firstGroupId, secondGroupId)
                    ((query.captured.queryObject["scheduleAt"] as Document)["\$gt"] is Date) shouldBe true
                    query.captured.sortObject["scheduleAt"] shouldBe 1
                    result.size shouldBe 2
                    result[firstGroupId.toHexString()]!!.id shouldBe firstScheduleId.toHexString()
                    result[firstGroupId.toHexString()]!!.session shouldBe 2L
                    result[secondGroupId.toHexString()]!!.id shouldBe secondScheduleId.toHexString()
                }
            }
        }
    }
}
