package net.noti_me.dymit.dymit_backend_api.units.feed.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.supports.FeedPullFixture
import net.noti_me.dymit.dymit_backend_api.feed.application.*
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.*

internal class FeedUnreadServicesTest : BehaviorSpec({
    listOf(0, 201).forEach { count ->
        Given("미읽음 수 조회 전 미반영 피드 $count 개") {
            Then("모든 배치의 저장과 이력 저장을 완료한 뒤 해당 회원 수를 계산한다") {
                val f = FeedPullFixture()
                repeat(count) { f.add(it + 1) }
                val service = GetUnreadUserFeedCountService(f.pull, f.users)
                service.execute(GetUnreadUserFeedCountCommand(f.member.toHexString())) shouldBe count.toLong()
                f.events.last() shouldBe "count"
                f.saved.size shouldBe count
                service.execute(GetUnreadUserFeedCountCommand(f.member.toHexString())) shouldBe count.toLong()
                f.saved.size shouldBe count
                verify(exactly = 2) { f.users.countUnreadByMemberId(f.member.toHexString()) }
            }
        }
        Given("모두 읽음 처리 전 미반영 피드 $count 개") {
            Then("pull 완료 후 단일 회원 일괄 처리를 호출하며 반복해도 읽음 상태를 유지한다") {
                val f = FeedPullFixture()
                repeat(count) { f.add(it + 1) }
                val service = MarkAllUserFeedsAsReadService(f.pull, f.users)
                service.execute(MarkAllUserFeedsAsReadCommand(f.member.toHexString()))
                f.events.last() shouldBe "read"
                f.saved.size shouldBe count
                f.saved.all { it.isRead } shouldBe true
                service.execute(MarkAllUserFeedsAsReadCommand(f.member.toHexString()))
                f.saved.size shouldBe count
                f.saved.all { it.isRead } shouldBe true
                verify(exactly = 2) { f.users.markAllUnreadAsReadByMemberId(f.member.toHexString()) }
                verify(exactly = 0) { f.users.findByMemberIdOrderByCreatedAtDesc(any(),any(),any()) }
            }
        }
    }
    listOf("group", "save", "history").forEach { failure ->
        Given("공유 pull의 $failure 단계 실패") {
            Then("count와 일괄 읽음 후속 처리를 모두 중단한다") {
                listOf(false,true).forEach { read ->
                    val f = FeedPullFixture()
                    f.add(1)
                    when(failure) {
                        "group" -> every { f.groups.findByGroupIdsOrderByIdAsc(any(),any(),any()) } throws IllegalStateException("pull failed")
                        "save" -> every { f.users.saveAll(any()) } throws IllegalStateException("save failed")
                        else -> every { f.histories.save(any()) } throws IllegalStateException("history failed")
                    }
                    shouldThrow<IllegalStateException> {
                        if(read) MarkAllUserFeedsAsReadService(f.pull,f.users).execute(MarkAllUserFeedsAsReadCommand(f.member.toHexString()))
                        else GetUnreadUserFeedCountService(f.pull,f.users).execute(GetUnreadUserFeedCountCommand(f.member.toHexString()))
                    }
                    verify(exactly=0) { f.users.countUnreadByMemberId(any()) }
                    verify(exactly=0) { f.users.markAllUnreadAsReadByMemberId(any()) }
                }
            }
        }
    }
    Given("동일 회원의 count 다음 모두 읽음 요청") {
        Then("기존 읽음 상태를 보존하며 이후 count는 0이 된다") {
            val f = FeedPullFixture()
            repeat(3) { f.add(it+1) }
            val count = GetUnreadUserFeedCountService(f.pull,f.users)
            count.execute(GetUnreadUserFeedCountCommand(f.member.toHexString())) shouldBe 3L
            f.saved[0].markAsRead()
            MarkAllUserFeedsAsReadService(f.pull,f.users).execute(MarkAllUserFeedsAsReadCommand(f.member.toHexString()))
            count.execute(GetUnreadUserFeedCountCommand(f.member.toHexString())) shouldBe 0L
        }
    }
})
