package net.noti_me.dymit.dymit_backend_api.units.study_schedule.adapter.out.persistence

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.statistics.MongoStudyScheduleStatisticsQueryAdapter
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsQuery
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant

internal class MongoStudyScheduleStatisticsQueryAdapterTest : BehaviorSpec({
    Given("수정 시각이 이전 계산일보다 오래됐지만 이번에 시작한 일정") {
        Then("수정 시각과 독립적인 도래 범위로 조회하고 참석 이력을 함께 반환한다") {
            val mongo = mockk<MongoTemplate>()
            val previous = Instant.parse("2026-09-20T15:00:00Z")
            val cutoff = Instant.parse("2026-09-27T15:00:00Z")
            val group = ObjectId()
            val member = ObjectId()
            val schedule = StudySchedule(
                id = ObjectId(), groupId = group, scheduleAt = cutoff.minusSeconds(1),
                createdAt = previous.minusSeconds(604800), updatedAt = previous.minusSeconds(604800)
            )
            val participant = ScheduleParticipant(
                scheduleId = schedule.id!!, memberId = member,
                createdAt = previous.minusSeconds(1), deletedAt = cutoff.plusSeconds(1)
            )
            val scheduleQueries = mutableListOf<Query>()
            every { mongo.find(any<Query>(), StudySchedule::class.java) } answers {
                val q = firstArg<Query>()
                scheduleQueries.add(q)
                if (q.queryObject.containsKey("scheduleAt")) listOf(schedule) else emptyList()
            }
            every { mongo.find(any<Query>(), ScheduleParticipant::class.java) } answers {
                if (firstArg<Query>().queryObject.containsKey("scheduleId")) listOf(participant) else emptyList()
            }
            val result = MongoStudyScheduleStatisticsQueryAdapter(mongo).loadChanged(
                StudyScheduleStatisticsQuery(group, member, previous.minusSeconds(604800), previous, cutoff, previous, cutoff)
            )
            result.requiresReplay shouldBe false
            result.sources.single().scheduleId shouldBe schedule.id
            result.sources.single().participations.single().deletedAt shouldBe cutoff.plusSeconds(1)
            val maturation = scheduleQueries.single { it.queryObject.containsKey("scheduleAt") }.queryObject
            maturation.containsKey("updatedAt") shouldBe false
            (maturation["scheduleAt"] as Document)["\$gte"] shouldBe previous
            (maturation["scheduleAt"] as Document)["\$lt"] shouldBe cutoff
        }
    }

    Given("이미 집계된 일정이 변경 검색의 겹침 구간에 다시 발견됨") {
        Then("전체 재계산 없이 기존 일정의 변경 전후 기여분을 비교할 수 있도록 반환한다") {
            val mongo = mockk<MongoTemplate>()
            val previous = Instant.parse("2026-09-20T15:00:00Z")
            val cutoff = Instant.parse("2026-09-27T15:00:00Z")
            val group = ObjectId()
            val member = ObjectId()
            val schedule = StudySchedule(
                id = ObjectId(), groupId = group, scheduleAt = previous.minusSeconds(1),
                createdAt = previous.minusSeconds(604800), updatedAt = previous
            )
            every { mongo.find(any<Query>(), StudySchedule::class.java) } answers {
                if (firstArg<Query>().queryObject.containsKey("updatedAt")) listOf(schedule) else emptyList()
            }
            every { mongo.find(any<Query>(), ScheduleParticipant::class.java) } returns emptyList()
            val result = MongoStudyScheduleStatisticsQueryAdapter(mongo).loadChanged(
                StudyScheduleStatisticsQuery(group, member, previous.minusSeconds(604800), previous, cutoff, previous, cutoff)
            )
            result.requiresReplay shouldBe false
            result.sources.single().scheduleId shouldBe schedule.id
        }
    }
})
