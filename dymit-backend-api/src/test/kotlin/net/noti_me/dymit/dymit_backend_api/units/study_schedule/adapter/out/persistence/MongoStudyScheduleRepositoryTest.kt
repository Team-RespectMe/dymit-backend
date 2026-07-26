package net.noti_me.dymit.dymit_backend_api.units.study_schedule.adapter.out.persistence

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.persistence.MongoStudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

internal class MongoStudyScheduleRepositoryTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val repository = MongoStudyScheduleRepository(mongoTemplate)

    init {
        afterTest {
            clearAllMocks()
        }

        given("그룹의 일정 목록 조회 요청이 주어지면") {
            `when`("저장소가 조회하면") {
                then("groupId 조건과 scheduleAt 내림차순 정렬을 MongoTemplate에 전달한다") {
                    val groupId = ObjectId.get()
                    val query = slot<Query>()
                    every { mongoTemplate.find(capture(query), StudySchedule::class.java) } returns emptyList()

                    repository.loadByGroupIdOrderByScheduleAtDesc(groupId)

                    verify(exactly = 1) { mongoTemplate.find(any(), StudySchedule::class.java) }
                    query.captured.queryObject["groupId"] shouldBe groupId
                    query.captured.sortObject["scheduleAt"] shouldBe -1
                }
            }
        }

        given("기간 및 커서 기반 일정 조회 요청이 주어지면") {
            `when`("저장소가 조회하면") {
                then("기간, 커서, 정렬, 제한 조건을 MongoTemplate에 전달한다") {
                    val start = LocalDateTime.of(2026, 7, 26, 0, 0)
                    val end = start.plusHours(15)
                    val cursor = ObjectId.get()
                    val query = slot<Query>()
                    every { mongoTemplate.find(capture(query), StudySchedule::class.java) } returns emptyList()

                    repository.findByScheduleAtBetweenCursorPagination(start, end, cursor, 100)

                    verify(exactly = 1) { mongoTemplate.find(any(), StudySchedule::class.java) }
                    query.captured.queryObject["scheduleAt"] shouldBe mapOf("${'$'}gte" to start, "${'$'}lt" to end)
                    query.captured.queryObject["_id"] shouldBe mapOf("${'$'}gt" to cursor)
                    query.captured.sortObject["_id"] shouldBe -1
                    query.captured.limit shouldBe 100
                }
            }
        }
    }
}
