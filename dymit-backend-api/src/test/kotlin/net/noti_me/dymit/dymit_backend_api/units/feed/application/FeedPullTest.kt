package net.noti_me.dymit.dymit_backend_api.units.feed.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.supports.FeedPullFixture
import net.noti_me.dymit.dymit_backend_api.feed.application.GetUserFeedsService
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.GetUserFeedsCommand
import org.bson.types.ObjectId

internal class FeedPullTest : BehaviorSpec({
    listOf(0, 100, 101, 201).forEach { count ->
        Given("미반영 그룹 피드 $count 개") {
            Then("모든 피드를 한 번씩 저장하고 처리한 마지막 ID까지 watermark를 진행한다") {
                val f = FeedPullFixture()
                repeat(count) { f.add(it + 1) }
                f.pull.pull(f.member.toHexString())
                f.saved.map { it.eventName }.toSet() shouldBe f.source.map { it.eventName }.toSet()
                f.saved.size shouldBe count
                f.saved.all { it.memberId == f.member && !it.isRead } shouldBe true
                f.queries.all { it.second == 101L } shouldBe true
                f.history!!.lastFeedId shouldBe (f.source.lastOrNull()?.id ?: ObjectId("000000000000000000000000"))
                f.watermarks shouldBe f.source.chunked(100).map { it.last().id }
                val before = f.saved.toList()
                f.pull.pull(f.member.toHexString())
                f.saved shouldBe before
            }
        }
    }
    listOf("excluded", "deleted", "mixed").forEach { kind ->
        Given("첫 배치가 모두 제외되는 유형 $kind") {
            Then("빈 저장 대상에서도 watermark를 전진하고 다음 배치의 유효 피드를 반영한다") {
                val f = FeedPullFixture()
                repeat(100) { i -> f.add(i + 1, excluded = kind == "excluded" || kind == "mixed" && i % 2 == 0, deleted = kind == "deleted" || kind == "mixed" && i % 2 == 1) }
                val accepted = f.add(101)
                f.pull.pull(f.member.toHexString())
                f.saved.map { it.eventName } shouldBe listOf(accepted.eventName)
                f.watermarks shouldBe listOf(f.source[99].id, accepted.id)
                f.queries.map { it.first } shouldBe listOf(ObjectId("000000000000000000000000"), f.source[99].id)
            }
        }
    }
    Given("가입 그룹이 없는 회원") {
        Then("빈 그룹 범위 조회로 끝내고 개인 피드와 watermark를 변경하지 않는다") {
            val f = FeedPullFixture()
            every { f.memberships.loadByMemberId(f.member.toHexString()) } returns
                net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto.FeedGroupMembershipDto(emptyList())
            every { f.groups.findByGroupIdsOrderByIdAsc(emptyList(), any(), 101) } returns emptyList()
            f.pull.pull(f.member.toHexString())
            f.saved shouldBe emptyList()
            f.watermarks shouldBe emptyList()
            verify(exactly = 0) { f.users.saveAll(any()) }
        }
    }
    Given("서로 다른 유효성의 피드가 섞인 배치") {
        Then("제외 및 삭제 피드를 건너뛰며 유효 피드의 metadata를 유지한다") {
            val f = FeedPullFixture()
            f.add(1, excluded = true)
            val accepted = f.add(2)
            f.add(3, deleted = true)
            f.pull.pull(f.member.toHexString())
            f.saved.single().messages shouldBe accepted.messages
            f.saved.single().createdAt shouldBe accepted.createdAt
            f.history!!.lastFeedId shouldBe f.source.last().id
        }
    }
    Given("조회 이력이 없는 신규 회원") {
        Then("가입일로 초기 위치를 저장한 후 피드를 반영한다") {
            val f = FeedPullFixture()
            f.history = null
            f.add(1)
            f.pull.pull(f.member.toHexString())
            f.watermarks.take(2) shouldBe listOf(null, ObjectId("000000000000000000000000"))
            f.saved.size shouldBe 1
            verify(exactly = 1) { f.members.loadById(f.member.toHexString()) }
        }
    }
    Given("개인 피드 저장 실패") {
        Then("실패한 배치의 watermark를 저장하지 않는다") {
            val f = FeedPullFixture()
            f.add(1)
            every { f.users.saveAll(any()) } throws IllegalStateException("save failed")
            shouldThrow<IllegalStateException> { f.pull.pull(f.member.toHexString()) }
            f.watermarks shouldBe emptyList()
            f.history!!.lastFeedId shouldBe ObjectId("000000000000000000000000")
        }
    }
    Given("기존 목록 API") {
        Then("동일 pull이 저장을 마친 뒤 기존 커서와 size로 개인 목록을 조회한다") {
            val f = FeedPullFixture()
            f.add(1)
            val cursor = ObjectId().toHexString()
            every { f.users.findByMemberIdOrderByCreatedAtDesc(f.member.toHexString(), cursor, 21) } answers {
                f.events += "list"
                f.saved.map { row ->
                    net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeed(
                        id = ObjectId(), memberId = row.memberId, iconType = row.iconType,
                        eventName = row.eventName, messages = row.messages, associates = row.associates,
                        isRead = row.isRead, createdAt = row.createdAt
                    )
                }
            }
            val result = GetUserFeedsService(f.pull, f.users).execute(GetUserFeedsCommand(f.member.toHexString(), cursor, 21))
            result.single().eventName shouldBe "EVENT_1"
            f.events shouldBe listOf("save", "history", "list")
        }
    }
})
