package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.supports.FrozenRosterStatisticsFixture
import java.time.Instant

internal class FrozenGroupRosterStatisticsTest : BehaviorSpec({
    Given("3주차 전 최초 요청이며 1주차 직후 탈퇴한 회원의 개인 원장은 이미 존재") {
        Then("신규 1·2회차 모두 활성 회원만 포함하며 탈퇴 원장은 합산하지 않는다") {
            val f = FrozenRosterStatisticsFixture()
            f.service.execute(GetMemberStatisticsCommand(f.other.memberId.toHexString(), f.group.toHexString(), f.other.memberId.toHexString()))
            f.stored.size shouldBe 0
            f.active = listOf(f.manager)
            f.all = listOf(f.manager, f.other.apply { markAsDeleted() })
            val result = f.execute()
            result.counts shouldBe f.managerCounts
            result.previousTaskSubmissionRate shouldBe 50.0
            result.latestSession shouldBe 2
            result.previousSession shouldBe 1
            result.statisticsAt shouldBe f.second.scheduleAt
            result.activeMemberCount shouldBe 1
            f.stored.values.map { it.membershipIds }.toSet() shouldBe setOf(listOf(f.manager.id))
        }
    }
    Given("2주차 전 요청으로 1회차 저장 후 회원이 탈퇴") {
        Then("1회차의 기여는 유지하며 신규 2회차만 탈퇴자를 제외한다") {
            val f = FrozenRosterStatisticsFixture()
            f.boundaries = listOf(f.first)
            f.execute().counts shouldBe StatisticsCounts(1, 5, 1, 5)
            f.active = listOf(f.manager)
            f.all = listOf(f.manager, f.other.apply { markAsDeleted() })
            f.boundaries = listOf(f.first, f.second)
            val result = f.execute()
            result.counts shouldBe f.managerCounts
            result.previousTaskSubmissionRate shouldBe 20.0
            result.previousScheduleAttendanceRate shouldBe 20.0
            f.stored[f.first.scheduleId]!!.membershipIds shouldBe listOf(f.manager.id, f.other.id)
            f.stored[f.second.scheduleId]!!.membershipIds shouldBe listOf(f.manager.id)
            f.ledgers.size shouldBe 2
        }
    }
    Given("계산 후 신규 가입 및 동일 사용자의 새 가입 관계") {
        Then("기존 회차 집합을 유지하며 새 회차에 새로운 가입 관계만 추가한다") {
            val f = FrozenRosterStatisticsFixture()
            f.boundaries = listOf(f.first)
            f.execute()
            val rejoined = net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember(
                id = org.bson.types.ObjectId(), groupId = f.group, memberId = f.other.memberId, createdAt = f.second.scheduleAt.minusSeconds(1))
            val newcomer = f.member()
            f.active = listOf(f.manager, rejoined, newcomer)
            f.all = listOf(f.manager, f.other.apply { markAsDeleted() }, rejoined, newcomer)
            f.boundaries = listOf(f.first, f.second)
            f.execute()
            f.stored[f.first.scheduleId]!!.membershipIds shouldBe listOf(f.manager.id, f.other.id)
            f.stored[f.second.scheduleId]!!.membershipIds shouldBe f.active.map { it.id }
        }
    }
    Given("저장된 회차 이후 탈퇴와 원천 변경") {
        Then("변경된 기여를 반영하되 기존 회차의 회원 집합은 유지한다") {
            val f = FrozenRosterStatisticsFixture()
            f.execute()
            f.active = listOf(f.manager)
            f.managerCounts = StatisticsCounts(2, 2, 2, 2)
            f.ledgers[f.manager.id!!] = f.ledgers[f.manager.id!!]!!.copy(version = 3, projectionId = "changed")
            f.execute().counts shouldBe StatisticsCounts(2, 5, 2, 5)
            f.stored.values.all { it.membershipIds == listOf(f.manager.id, f.other.id) } shouldBe true
        }
    }
    Given("최초 그룹 회차 생성 CAS에서 다른 요청이 먼저 다른 회원 집합을 저장") {
        Then("승자가 저장한 집합을 유지하여 재시도한다") {
            val f = FrozenRosterStatisticsFixture()
            f.boundaries = listOf(f.first)
            var conflict = true
            every { f.repository.compareAndSetGroupSession(any(), any()) } answers {
                val next = secondArg<GroupSessionStatistics>()
                if (conflict) {
                    conflict = false
                    f.stored[next.scheduleId] = next.copy(membershipIds = listOf(f.manager.id!!), sourceProjectionToken = "winner")
                    false
                } else {
                    f.stored[next.scheduleId] = next
                    true
                }
            }
            f.execute().counts shouldBe f.managerCounts
            f.stored[f.first.scheduleId]!!.membershipIds shouldBe listOf(f.manager.id)
        }
    }
    Given("활성 회원 조회가 빈 집합을 반환") {
        Then("새 회차는 구형 문서와 구별되는 빈 집합과 0을 저장한다") {
            val f = FrozenRosterStatisticsFixture()
            every { f.members.findActiveByGroupId(f.group, null, any()) } returns emptyList()
            f.execute().counts shouldBe StatisticsCounts()
            f.stored.values.all { it.membershipIds == emptyList<Any>() } shouldBe true
            verify(exactly = 0) { f.refresh.execute(any()) }
        }
    }
    Given("회원 집합을 기록하지 않은 기존 그룹 문서") {
        Then("복원 불가능한 저장값을 덮어쓰지 않고 현재 인원만 반환한다") {
            val f = FrozenRosterStatisticsFixture()
            f.boundaries.forEach { b ->
                f.stored[b.scheduleId] = GroupSessionStatistics(groupId = f.group, scheduleId = b.scheduleId,
                    session = b.session, scheduleAt = b.scheduleAt, statisticsAt = b.scheduleAt,
                    counts = StatisticsCounts(7, 10, 8, 10), sourceProjectionToken = "legacy", version = 9, updatedAt = Instant.EPOCH)
            }
            val originalAt = f.second.scheduleAt
            f.boundaries = listOf(f.first.copy(session = 101), f.second.copy(session = 202, scheduleAt = originalAt.plusSeconds(60)))
            val result = f.execute()
            result.counts shouldBe StatisticsCounts(7, 10, 8, 10)
            result.latestSession shouldBe 2
            result.previousSession shouldBe 1
            result.statisticsAt shouldBe originalAt
            f.saved.size shouldBe 0
            f.stored.values.all { it.membershipIds == null && it.version == 9L } shouldBe true
            verify(exactly = 0) { f.refresh.execute(any()) }
        }
    }
    Given("그룹에 포함되지 않은 탈퇴 회원 원장이 최종 확인 사이에 갱신") {
        Then("고정 회원 집합 밖의 버전 변경 때문에 재시도하거나 충돌하지 않는다") {
            val f = FrozenRosterStatisticsFixture()
            f.active = listOf(f.manager)
            f.ledgers[f.other.id!!] = net.noti_me.dymit.dymit_backend_api.supports.sessionLedger(f.group, f.other.id!!, f.second.scheduleId)
            var reads = 0L
            every { f.repository.findLedgersByGroupId(f.group) } answers {
                reads++
                f.ledgers[f.other.id!!] = f.ledgers[f.other.id!!]!!.copy(version = reads, projectionId = "excluded-$reads")
                f.ledgers.values.toList()
            }
            f.execute().counts shouldBe f.managerCounts
            reads shouldBe 2L
            f.saved.size shouldBe 2
            f.stored.values.all { it.membershipIds == listOf(f.manager.id) } shouldBe true
        }
    }

})
