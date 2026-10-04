package net.noti_me.dymit.dymit_backend_api.units.calendar

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.calendar.adapter.out.study_group.MongoCalendarStudyGroupAdapter
import net.noti_me.dymit.dymit_backend_api.calendar.adapter.out.study_schedule.MongoCalendarStudyScheduleAdapter
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant

internal class CalendarPersistenceAdapterTest : BehaviorSpec() {

    private val mongo = mockk<MongoTemplate>()

    init {
        afterEach { clearAllMocks() }
        Given("현재 회원의 소속 그룹 조회") {
            Then("탈퇴 관계와 삭제 그룹을 제외하는 제한 없는 쿼리로 이름을 매핑한다") {
                val memberId = ObjectId.get()
                val groupId = ObjectId.get()
                val membershipQuery = slot<Query>()
                val groupQuery = slot<Query>()
                every { mongo.find(capture(membershipQuery), StudyGroupMember::class.java) } returns listOf(
                    StudyGroupMember(groupId = groupId, memberId = memberId),
                    StudyGroupMember(groupId = groupId, memberId = memberId)
                )
                every { mongo.find(capture(groupQuery), StudyGroup::class.java) } returns listOf(StudyGroup(id = groupId, name = "알고리즘"))
                val result = MongoCalendarStudyGroupAdapter(mongo).loadActiveGroupsByMemberId(memberId.toHexString())
                result.single().groupId shouldBe groupId.toHexString()
                result.single().groupName shouldBe "알고리즘"
                membershipQuery.captured.queryObject shouldBe org.bson.Document(mapOf("memberId" to memberId, "isDeleted" to false))
                groupQuery.captured.queryObject["_id"] shouldBe mapOf("${'$'}in" to listOf(groupId))
                groupQuery.captured.queryObject["isDeleted"] shouldBe false
                groupQuery.captured.sortObject.isEmpty() shouldBe true
                membershipQuery.captured.sortObject.isEmpty() shouldBe true
                membershipQuery.captured.limit shouldBe 0
                groupQuery.captured.limit shouldBe 0
                verify(exactly = 1) { mongo.find(any(), StudyGroupMember::class.java) }
                verify(exactly = 1) { mongo.find(any(), StudyGroup::class.java) }
            }
            Then("활성 소속이 없으면 그룹 문서를 조회하지 않는다") {
                every { mongo.find(any<Query>(), StudyGroupMember::class.java) } returns emptyList()
                MongoCalendarStudyGroupAdapter(mongo).loadActiveGroupsByMemberId(ObjectId.get().toHexString()) shouldBe emptyList()
                verify(exactly = 0) { mongo.find(any(), StudyGroup::class.java) }
            }
        }
        Given("여러 소속 그룹의 일정 기간 조회") {
            Then("소속과 삭제 여부 및 반개구간으로 한 번 조회하고 시각과 식별자를 정렬한다") {
                val ids = listOf(ObjectId.get(), ObjectId.get())
                val start = Instant.parse("2026-10-01T15:00:00Z")
                val end = start.plusSeconds(86400)
                val captured = slot<Query>()
                val participants = slot<Query>()
                val memberId = ObjectId.get()
                val schedule = StudySchedule(id = ObjectId.get(), groupId = ids[0], title = "일정", scheduleAt = start)
                every { mongo.find(capture(participants), ScheduleParticipant::class.java) } returns listOf(ScheduleParticipant(scheduleId = schedule.id!!, memberId = memberId))
                every { mongo.find(capture(captured), StudySchedule::class.java) } returns listOf(schedule)
                val result = MongoCalendarStudyScheduleAdapter(mongo).loadActiveSchedules(memberId.toHexString(), ids.map { it.toHexString() }, start, end)
                captured.captured.queryObject["groupId"] shouldBe mapOf("${'$'}in" to ids)
                captured.captured.queryObject["scheduleAt"] shouldBe mapOf("${'$'}gte" to start, "${'$'}lt" to end)
                captured.captured.queryObject["isDeleted"] shouldBe false
                captured.captured.queryObject.keys shouldBe setOf("groupId", "_id", "scheduleAt", "isDeleted")
                captured.captured.queryObject["_id"] shouldBe mapOf("${'$'}in" to listOf(schedule.id))
                participants.captured.queryObject shouldBe org.bson.Document("memberId", memberId)
                    .append("isDeleted", mapOf("${'$'}ne" to true))
                participants.captured.limit shouldBe 0
                captured.captured.sortObject.isEmpty() shouldBe true
                participants.captured.sortObject.isEmpty() shouldBe true
                captured.captured.limit shouldBe 0
                result.single().id shouldBe schedule.identifier
                result.single().groupId shouldBe ids[0].toHexString()
                result.single().title shouldBe "일정"
                result.single().eventAt shouldBe start
                verify(exactly = 1) { mongo.find(any(), StudySchedule::class.java) }
            }
            listOf("미참가", "다른 회원만 참가", "본인 참가 취소").forEach { situation ->
                Then("$situation 상태에서는 소속 그룹의 일정을 표시하지 않는다") {
                    every { mongo.find(any<Query>(), ScheduleParticipant::class.java) } returns emptyList()
                    MongoCalendarStudyScheduleAdapter(mongo).loadActiveSchedules(
                        ObjectId.get().toHexString(), listOf(ObjectId.get().toHexString()), Instant.EPOCH, Instant.EPOCH.plusSeconds(1)
                    ) shouldBe emptyList()
                    verify(exactly = 0) { mongo.find(any(), StudySchedule::class.java) }
                }
            }
            Then("소속 그룹이 없으면 MongoDB를 조회하지 않는다") {
                MongoCalendarStudyScheduleAdapter(mongo).loadActiveSchedules(ObjectId.get().toHexString(), emptyList(), Instant.EPOCH, Instant.EPOCH.plusSeconds(1)) shouldBe emptyList()
                verify { mongo wasNot Called }
            }
        }
    }
}
