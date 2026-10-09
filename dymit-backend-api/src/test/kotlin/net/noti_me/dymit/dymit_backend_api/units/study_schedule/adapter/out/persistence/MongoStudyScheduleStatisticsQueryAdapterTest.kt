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
    Given("수정 시각이 이전 계산일보다 오래됐지만 현재 회차 시작 시각에 도래한 일정") {
        Then("수정 시각과 독립적인 도래 범위로 조회하고 참석 이력을 함께 반환한다") {
            val mongo = mockk<MongoTemplate>()
            val previous = Instant.parse("2026-09-20T15:00:00Z")
            val cutoff = Instant.parse("2026-09-27T15:00:00Z")
            val group = ObjectId()
            val member = ObjectId()
            val schedule = StudySchedule(
                id = ObjectId(), groupId = group, scheduleAt = cutoff,
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
            (maturation["scheduleAt"] as Document)["\$gt"] shouldBe previous
            (maturation["scheduleAt"] as Document)["\$lte"] shouldBe cutoff
        }
    }

    Given("이미 집계된 일정이 변경 검색의 겹침 구간에 다시 발견됨") {
        Then("이미 집계된 일정 변경은 과거 회차 재투영 대상으로 반환한다") {
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
            result.requiresReplay shouldBe true
            result.sources.single().scheduleId shouldBe schedule.id
        }
    }

    Given("여러 그룹의 진행 일정 경계 조회") {
        Then("현재 시각 이전의 유효 일정만 시간과 ID 순으로 배치 조회하며 중복 회차를 보존한다") {
            val mongo = mockk<MongoTemplate>()
            val group = ObjectId()
            val other = ObjectId()
            val observed = Instant.parse("2026-10-06T01:00:00Z")
            val captured = io.mockk.slot<Query>()
            val ids = listOf(ObjectId("000000000000000000000001"), ObjectId("000000000000000000000002"))
            every { mongo.find(capture(captured), StudySchedule::class.java) } returns ids.map {
                StudySchedule(id = it, groupId = group, session = 7, scheduleAt = observed.minusSeconds(1))
            }
            val result = MongoStudyScheduleStatisticsQueryAdapter(mongo).loadSessionBoundaries(listOf(group, other), observed)
            result.map { it.scheduleId } shouldBe ids
            result.map { it.session } shouldBe listOf(7L, 7L)
            (captured.captured.queryObject["groupId"] as Document)["\$in"] shouldBe listOf(group, other)
            (captured.captured.queryObject["scheduleAt"] as Document)["\$lt"] shouldBe observed
            (captured.captured.queryObject["isDeleted"] as Document)["\$ne"] shouldBe true
            captured.captured.sortObject shouldBe Document("scheduleAt", 1).append("_id", 1)
        }
    }

    Given("미래 일정만 존재하는 그룹의 일정 존재 여부") {
        Then("과거 시각 제한 없이 삭제되지 않은 일정 존재를 배치 조회한다") {
            val mongo = mockk<MongoTemplate>()
            val group = ObjectId()
            val captured = io.mockk.slot<Query>()
            every { mongo.findDistinct(capture(captured), "groupId", StudySchedule::class.java, ObjectId::class.java) } returns listOf(group)
            MongoStudyScheduleStatisticsQueryAdapter(mongo).loadGroupIdsHavingSchedule(listOf(group)) shouldBe setOf(group)
            captured.captured.queryObject.containsKey("scheduleAt") shouldBe false
            (captured.captured.queryObject["isDeleted"] as Document)["\$ne"] shouldBe true
        }
    }
})
