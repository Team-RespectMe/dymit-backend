package net.noti_me.dymit.dymit_backend_api.units.study_schedule.adapter.out.persistence

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.persistence.MongoScheduleCommentRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleComment
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

internal class MongoScheduleCommentRepositoryTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val repository = MongoScheduleCommentRepository(mongoTemplate)

    init {
        afterTest {
            clearAllMocks()
        }

        given("일정 댓글 조회 요청이 주어지면") {
            `when`("커서와 함께 댓글을 조회하면") {
                then("일정 ID와 커서 조건 및 내림차순 제한을 적용한다") {
                    val scheduleId = ObjectId.get()
                    val cursor = ObjectId.get()
                    val query = slot<Query>()
                    every { mongoTemplate.find(capture(query), ScheduleComment::class.java) } returns emptyList()

                    repository.findByScheduleId(scheduleId, cursor, 20)

                    verify(exactly = 1) { mongoTemplate.find(any(), ScheduleComment::class.java) }
                    query.captured.queryObject["scheduleId"] shouldBe scheduleId
                    query.captured.queryObject["_id"] shouldBe mapOf("${'$'}lt" to cursor)
                    query.captured.limit shouldBe 20
                    query.captured.sortObject["id"] shouldBe -1
                }
            }
        }

        given("일정 댓글 삭제 요청이 주어지면") {
            `when`("저장소가 삭제를 수행하면") {
                then("댓글 컬렉션 타입과 _id 조건만 MongoTemplate에 전달한다") {
                    val commentId = ObjectId.get()
                    val query = slot<Query>()
                    every { mongoTemplate.remove(capture(query), ScheduleComment::class.java) } returns mockk(relaxed = true)

                    repository.deleteById(commentId)

                    verify(exactly = 1) { mongoTemplate.remove(any(), ScheduleComment::class.java) }
                    query.captured.queryObject["_id"] shouldBe commentId
                }
            }
        }
    }
}
