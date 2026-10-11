package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.supports.MemberStatisticsRankingFixture
import org.bson.types.ObjectId

internal class MemberStatisticsRankingTest : BehaviorSpec({

    Given("ID 순서와 제출률·참석률 합계 순위가 서로 다른 구성원") {
        Then("합계가 먼저이고 같은 합계에서는 참석률이 높은 구성원이 먼저다") {
            val f = MemberStatisticsRankingFixture()
            val highTask = f.addMember(90.0, 0.0)
            val highAttendance = f.addMember(0.0, 80.0)
            val balanced = f.addMember(40.0, 50.0)
            f.execute().map { it.membershipId } shouldBe listOf(balanced.identifier, highTask.identifier, highAttendance.identifier)
        }
    }

    Given("소수 두 자리 비율의 합계가 수학적으로 같은 구성원") {
        Then("부동소수점 덧셈 오차로 동률을 깨지 않고 참석률로 순서를 결정한다") {
            val f = MemberStatisticsRankingFixture()
            val artifact = f.addMember(0.10, 0.20)
            val attendance = f.addMember(0.00, 0.30)
            f.execute().map { it.membershipId } shouldBe listOf(attendance.identifier, artifact.identifier)
        }
    }

    Given("두 정렬 값이 완전히 같은 구성원이 여러 페이지에 걸침") {
        Then("안정 정렬로 입력 순서를 보존하고 정적인 페이지 전체에 중복이나 누락이 없다") {
            val f = MemberStatisticsRankingFixture()
            val entries = listOf(3, 1, 5, 2, 4).map { index ->
                f.addMember(33.33, 66.67, ObjectId(index.toString(16).padStart(24, '0')))
            }
            val collected = mutableListOf<String>()
            var cursor: String? = null
            do {
                val candidates = f.execute(cursor, 2)
                val page = candidates.take(2)
                collected.addAll(page.map { it.membershipId })
                cursor = page.lastOrNull()?.membershipId
            } while (candidates.size > 2)
            collected shouldBe entries.map { it.identifier }
            collected.distinct().size shouldBe entries.size
            f.internalQueries.all { it.first == null && it.second == 200 } shouldBe true
        }
    }

    Given("활성 구성원이 내부 배치 크기보다 많고 마지막 ID의 점수가 가장 높음") {
        Then("모든 내부 배치를 갱신한 후 전체 순위를 만들며 외부 커서를 ID 대소 필터로 사용하지 않는다") {
            val f = MemberStatisticsRankingFixture()
            val entries = (1..201).map { index ->
                f.addMember(if (index == 201) 100.0 else 1.0, if (index == 201) 100.0 else 1.0,
                    ObjectId(index.toString(16).padStart(24, '0')))
            }
            f.execute(size = 1).map { it.membershipId } shouldBe listOf(entries.last().identifier, entries.first().identifier)
            f.internalQueries shouldBe listOf(null to 200, entries[199].id to 200)
            f.commands.size shouldBe 201
            f.commands.map { it.boundaries }.distinct() shouldBe listOf(f.boundaries)
            f.commands.map { it.observedAt }.distinct().size shouldBe 1
            f.internalQueries.clear()
            f.execute(entries.last().identifier, 1).map { it.membershipId } shouldBe entries.take(2).map { it.identifier }
            f.internalQueries shouldBe listOf(null to 200, entries[199].id to 200)
        }
    }

    Given("구성원 수가 내부 배치 크기의 정확한 배수") {
        Then("마지막 빈 내부 조회로 종료하고 모든 구성원을 한 번씩 갱신한다") {
            val f = MemberStatisticsRankingFixture()
            val entries = List(200) { f.addMember() }
            f.execute(size = 1).size shouldBe 2
            f.internalQueries shouldBe listOf(null to 200, entries.last().id to 200)
            f.commands.map { it.membershipId } shouldBe entries.map { it.identifier }
        }
    }

    Given("점수 순위의 마지막 구성원을 가리키는 커서") {
        Then("뒤 구성원이 없으면 빈 페이지를 반환한다") {
            val f = MemberStatisticsRankingFixture()
            f.addMember(90.0, 90.0)
            val last = f.addMember(10.0, 10.0)
            f.execute(last.identifier) shouldBe emptyList()
        }
    }

    Given("대문자 16진수로 전달한 유효한 가입 ID 커서") {
        Then("ID를 정규화하여 점수 순위에서 다음 구성원을 찾는다") {
            val f = MemberStatisticsRankingFixture()
            val first = f.addMember(90.0, 90.0, ObjectId("abcdefabcdefabcdefabcdef"))
            val next = f.addMember(10.0, 10.0)
            f.execute(first.identifier.uppercase()).map { it.membershipId } shouldBe listOf(next.identifier)
        }
    }

    Given("진행 회차가 없어 모든 구성원의 통계가 0") {
        Then("통계 0과 null 기준을 유지하며 안정 정렬로 목록을 반환한다") {
            val f = MemberStatisticsRankingFixture()
            f.boundaries = emptyList()
            val entries = List(3) { f.addMember() }
            val result = f.execute()
            result.map { it.membershipId } shouldBe entries.map { it.identifier }
            result.all { it.latestSession == null && it.statisticsAt == null &&
                it.taskSubmissionRate == 0.0 && it.scheduleAttendanceRate == 0.0 } shouldBe true
        }
    }

    Given("활성 구성원이 없는 목록") {
        Then("통계 갱신 없이 빈 결과를 반환한다") {
            val f = MemberStatisticsRankingFixture()
            f.execute() shouldBe emptyList()
            verify { f.refresh wasNot Called }
        }
    }

    Given("가입 ID 형식이 아닌 커서") {
        Then("활성 구성원 스캔과 통계 갱신 전에 400 오류를 반환한다") {
            val f = MemberStatisticsRankingFixture()
            f.addMember()
            shouldThrow<BadRequestException> { f.execute("invalid-membership-id") }
            f.internalQueries shouldBe emptyList()
            verify { f.refresh wasNot Called }
            verify { f.schedules wasNot Called }
        }
    }

    listOf("누락", "다른 그룹", "탈퇴").forEach { state ->
        Given("요청 그룹 활성 목록에 없는 $state 가입 커서") {
            Then("전체 순위 갱신 전에 커서가 유효하지 않다는 400 오류를 반환한다") {
                val f = MemberStatisticsRankingFixture()
                f.addMember()
                val cursor = ObjectId()
                every { f.members.findByIdIncludingDeleted(cursor) } returns when (state) {
                    "누락" -> null
                    "다른 그룹" -> StudyGroupMember(id = cursor, groupId = ObjectId())
                    else -> StudyGroupMember(id = cursor, groupId = f.group, isDeleted = true)
                }
                shouldThrow<BadRequestException> { f.execute(cursor.toHexString()) }
                verify { f.refresh wasNot Called }
                verify { f.schedules wasNot Called }
            }
        }
    }
})
