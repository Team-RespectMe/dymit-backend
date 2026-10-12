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
            Then("참가 조건 없이 활성 일정을 먼저 조회하고 반환 일정 ID로만 참가자를 일괄 조회한다") {
                val ids = listOf(ObjectId.get(), ObjectId.get())
                val start = Instant.parse("2026-10-01T15:00:00Z")
                val end = start.plusSeconds(86400)
                val captured = slot<Query>()
                val participants = slot<Query>()
                val memberId = ObjectId.get()
                val rows = listOf(
                    StudySchedule(id = ObjectId.get(), groupId = ids[0], title = "일정", scheduleAt = start),
                    StudySchedule(id = ObjectId.get(), groupId = ids[1], title = "다른 일정", scheduleAt = start.plusSeconds(1))
                )
                every { mongo.find(capture(captured), StudySchedule::class.java) } returns rows
                every { mongo.find(capture(participants), ScheduleParticipant::class.java) } returns emptyList()
                val result = MongoCalendarStudyScheduleAdapter(mongo).loadActiveSchedules(memberId.toHexString(), ids.map { it.toHexString() }, start, end)
                captured.captured.queryObject["groupId"] shouldBe mapOf("${'$'}in" to ids)
                captured.captured.queryObject["scheduleAt"] shouldBe mapOf("${'$'}gte" to start, "${'$'}lt" to end)
                captured.captured.queryObject["isDeleted"] shouldBe false
                captured.captured.queryObject.keys shouldBe setOf("groupId", "scheduleAt", "isDeleted")
                participants.captured.queryObject shouldBe org.bson.Document("scheduleId", org.bson.Document("${'$'}in", rows.map { it.id }))
                    .append("isDeleted", org.bson.Document("${'$'}ne", true))
                participants.captured.limit shouldBe 0
                captured.captured.sortObject.isEmpty() shouldBe true
                captured.captured.limit shouldBe 0
                result.map { it.id } shouldBe rows.map { it.identifier }
                result.map { it.participantCount } shouldBe listOf(0L, 0L)
                result.map { it.isParticipant } shouldBe listOf(false, false)
                verifyOrder {
                    mongo.find(any<Query>(), StudySchedule::class.java)
                    mongo.find(any<Query>(), ScheduleParticipant::class.java)
                }
                verify(exactly = 1) { mongo.find(any<Query>(), StudySchedule::class.java) }
                verify(exactly = 1) { mongo.find(any<Query>(), ScheduleParticipant::class.java) }
            }
            listOf("미참가", "다른 회원만 참가", "본인 참가 취소", "본인 활성 중복 참가").forEach { situation ->
                Then("$situation 상태에서도 일정은 보이고 활성 회원 수와 본인 등록 여부만 달라진다") {
                    val member = ObjectId()
                    val other = ObjectId()
                    val group = ObjectId()
                    val schedule = StudySchedule(id = ObjectId(), groupId = group, title = "일정", scheduleAt = Instant.EPOCH)
                    val registrations = when (situation) {
                        "미참가" -> emptyList()
                        "다른 회원만 참가" -> listOf(ScheduleParticipant(scheduleId = schedule.id!!, memberId = other), ScheduleParticipant(scheduleId = schedule.id!!, memberId = other))
                        "본인 참가 취소" -> listOf(ScheduleParticipant(scheduleId = schedule.id!!, memberId = member, isDeleted = true), ScheduleParticipant(scheduleId = schedule.id!!, memberId = other))
                        else -> listOf(ScheduleParticipant(scheduleId = schedule.id!!, memberId = member), ScheduleParticipant(scheduleId = schedule.id!!, memberId = member), ScheduleParticipant(scheduleId = schedule.id!!, memberId = other))
                    }
                    val participantQuery = slot<Query>()
                    every { mongo.find(any<Query>(), StudySchedule::class.java) } returns listOf(schedule)
                    every { mongo.find(capture(participantQuery), ScheduleParticipant::class.java) } returns registrations.filterNot { it.isDeleted }
                    val result = MongoCalendarStudyScheduleAdapter(mongo).loadActiveSchedules(member.toHexString(), listOf(group.toHexString()), Instant.EPOCH, Instant.EPOCH.plusSeconds(1)).single()
                    result.id shouldBe schedule.identifier
                    result.participantCount shouldBe when(situation) { "미참가" -> 0L; "본인 활성 중복 참가" -> 2L; else -> 1L }
                    result.isParticipant shouldBe (situation == "본인 활성 중복 참가")
                    participantQuery.captured.queryObject["isDeleted"] shouldBe mapOf("${'$'}ne" to true)
                    participantQuery.captured.queryObject.containsKey("memberId") shouldBe false
                }
            }
            Then("여러 일정의 참가 집계가 섞이지 않고 한 배치로 distinct 회원을 계산한다") {
                val member = ObjectId()
                val other = ObjectId()
                val group = ObjectId()
                val rows = List(3) { StudySchedule(id = ObjectId(), groupId = group, title = "일정 $it", scheduleAt = Instant.EPOCH) }
                every { mongo.find(any<Query>(), StudySchedule::class.java) } returns rows
                every { mongo.find(any<Query>(), ScheduleParticipant::class.java) } returns listOf(
                    ScheduleParticipant(scheduleId = rows[0].id!!, memberId = other),
                    ScheduleParticipant(scheduleId = rows[0].id!!, memberId = other),
                    ScheduleParticipant(scheduleId = rows[1].id!!, memberId = member),
                    ScheduleParticipant(scheduleId = rows[1].id!!, memberId = other)
                )
                val result = MongoCalendarStudyScheduleAdapter(mongo).loadActiveSchedules(member.toHexString(), listOf(group.toHexString()), Instant.EPOCH, Instant.EPOCH.plusSeconds(1))
                result.map { it.participantCount } shouldBe listOf(1L, 2L, 0L)
                result.map { it.isParticipant } shouldBe listOf(false, true, false)
                verify(exactly = 1) { mongo.find(any<Query>(), ScheduleParticipant::class.java) }
            }
            Then("일정이 없으면 참가자를 조회하지 않는다") {
                every { mongo.find(any<Query>(), StudySchedule::class.java) } returns emptyList()
                MongoCalendarStudyScheduleAdapter(mongo).loadActiveSchedules(ObjectId().toHexString(), listOf(ObjectId().toHexString()), Instant.EPOCH, Instant.EPOCH.plusSeconds(1)) shouldBe emptyList()
                verify(exactly = 0) { mongo.find(any<Query>(), ScheduleParticipant::class.java) }
            }
            Then("소속 그룹이 없으면 MongoDB를 조회하지 않는다") {
                MongoCalendarStudyScheduleAdapter(mongo).loadActiveSchedules(ObjectId.get().toHexString(), emptyList(), Instant.EPOCH, Instant.EPOCH.plusSeconds(1)) shouldBe emptyList()
                verify { mongo wasNot Called }
            }
        }
    }
}
