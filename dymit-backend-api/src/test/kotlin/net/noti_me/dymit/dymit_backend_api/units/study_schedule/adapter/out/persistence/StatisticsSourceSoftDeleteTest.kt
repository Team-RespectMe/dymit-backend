package net.noti_me.dymit.dymit_backend_api.units.study_schedule.adapter.out.persistence

import com.mongodb.client.result.UpdateResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.persistence.MongoStudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.persistence.MongoScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.persistence.MongoStudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant

internal class StatisticsSourceSoftDeleteTest : BehaviorSpec({
    listOf("가입 관계", "일정", "참가 기록").forEach { source ->
        Given("$source 삭제 요청에 기존 문서가 없음") {
            Then("새 문서를 삽입하지 않고 false를 반환한다") {
                val mongo = mockk<MongoTemplate>()
                val query = slot<Query>()
                val id = ObjectId()
                stubDelete(mongo, source, query, slot(), 0)
                delete(mongo, source, id) shouldBe false
                query.captured.queryObject["_id"] shouldBe id
                (query.captured.queryObject["isDeleted"] as Document)["\$ne"] shouldBe true
            }
        }
        Given("$source 삭제 중 다른 요청이 업무 필드를 수정함") {
            Then("삭제 상태와 시각만 수정하고 오래된 엔티티 전체를 덮어쓰지 않는다") {
                val mongo = mockk<MongoTemplate>()
                val query = slot<Query>()
                val update = slot<Update>()
                stubDelete(mongo, source, query, update, 1)
                delete(mongo, source, ObjectId()) shouldBe true
                val fields = update.captured.updateObject["\$set"] as Document
                fields.keys shouldBe setOf("isDeleted", "deletedAt", "updatedAt")
                fields["isDeleted"] shouldBe true
                fields["deletedAt"] shouldBe fields["updatedAt"]
                update.captured.updateObject.containsKey("\$setOnInsert") shouldBe false
            }
        }
    }
}) {
    companion object {
        private fun stubDelete(
            mongo: MongoTemplate,
            source: String,
            query: io.mockk.CapturingSlot<Query>,
            update: io.mockk.CapturingSlot<Update>,
            modified: Long
        ) {
            val result = UpdateResult.acknowledged(modified, modified, null)
            when (source) {
                "가입 관계" -> every { mongo.updateFirst(capture(query), capture(update), StudyGroupMember::class.java) } returns result
                "일정" -> every { mongo.updateFirst(capture(query), capture(update), StudySchedule::class.java) } returns result
                else -> every { mongo.updateFirst(capture(query), capture(update), ScheduleParticipant::class.java) } returns result
            }
        }

        private fun delete(
            mongo: MongoTemplate,
            source: String,
            id: ObjectId
        ): Boolean = when (source) {
            "가입 관계" -> MongoStudyGroupMemberRepository(mongo).delete(StudyGroupMember(id = id))
            "일정" -> MongoStudyScheduleRepository(mongo).delete(StudySchedule(id = id, scheduleAt = Instant.EPOCH))
            else -> MongoScheduleParticipantRepository(mongo).delete(ScheduleParticipant(id = id))
        }
    }
}
