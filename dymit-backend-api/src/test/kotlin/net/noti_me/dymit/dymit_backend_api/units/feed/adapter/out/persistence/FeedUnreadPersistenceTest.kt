package net.noti_me.dymit.dymit_backend_api.units.feed.adapter.out.persistence

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import com.mongodb.client.result.UpdateResult
import net.noti_me.dymit.dymit_backend_api.feed.adapter.out.persistence.*
import net.noti_me.dymit.dymit_backend_api.feed.domain.*
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant

internal class FeedUnreadPersistenceTest : BehaviorSpec({
    Given("로그인 회원의 미읽음 수 쿼리") {
        Then("memberId와 isRead=false 조건으로 count를 한 번 호출한다") {
            val template=mockk<MongoTemplate>()
            val query=slot<Query>()
            val member=ObjectId()
            every { template.count(capture(query),UserFeed::class.java) } returns 0L
            MongoUserFeedAdapter(template).countUnreadByMemberId(member.toHexString()) shouldBe 0L
            query.captured.queryObject shouldBe Document("memberId",member).append("isRead",false)
            verify(exactly=1) { template.count(any<Query>(),UserFeed::class.java) }
            confirmVerified(template)
        }
    }
    Given("로그인 회원의 전체 미읽음 처리") {
        Then("이미 읽음과 다른 회원을 제외하고 updatedAt을 포함한 단일 updateMulti를 호출한다") {
            val template=mockk<MongoTemplate>()
            val query=slot<Query>()
            val update=slot<Update>()
            val member=ObjectId()
            every { template.updateMulti(capture(query),capture(update),UserFeed::class.java) } returns UpdateResult.acknowledged(3,3,null)
            val before=Instant.now()
            MongoUserFeedAdapter(template).markAllUnreadAsReadByMemberId(member.toHexString())
            val after=Instant.now()
            query.captured.queryObject shouldBe Document("memberId",member).append("isRead",false)
            val set=update.captured.updateObject["\$set"] as Document
            set.keys shouldBe setOf("isRead","updatedAt")
            set["isRead"] shouldBe true
            val at=set["updatedAt"] as Instant
            (!at.isBefore(before) && !at.isAfter(after)) shouldBe true
            verify(exactly=1) { template.updateMulti(any<Query>(),any<Update>(),UserFeed::class.java) }
            confirmVerified(template)
        }
    }
    Given("그룹 피드 pull의 복합 그룹 범위와 이전 watermark") {
        Then("ID가 더 큰 항목을 오름차순으로 101개까지 조회한다") {
            val template=mockk<MongoTemplate>()
            val query=slot<Query>()
            every { template.find(capture(query),GroupFeed::class.java) } returns emptyList()
            val groups=List(2){ObjectId()}
            val cursor=ObjectId()
            MongoGroupFeedAdapter(template).findByGroupIdsOrderByIdAsc(groups,cursor,101) shouldBe emptyList()
            query.captured.queryObject shouldBe Document("groupId",Document("\$in",groups)).append("_id",Document("\$gt",cursor))
            query.captured.sortObject shouldBe Document("_id",1)
            query.captured.limit shouldBe 101
        }
    }
    Given("기존 개인 피드 목록 쿼리") {
        Then("회원 조건과 과거 커서 및 최신 생성시각 정렬을 유지한다") {
            val template=mockk<MongoTemplate>()
            val query=slot<Query>()
            every { template.find(capture(query),UserFeed::class.java) } returns emptyList()
            val member=ObjectId(); val cursor=ObjectId()
            MongoUserFeedAdapter(template).findByMemberIdOrderByCreatedAtDesc(member.toHexString(),cursor.toHexString(),21)
            query.captured.queryObject shouldBe Document("memberId",member).append("_id",Document("\$lt",cursor))
            query.captured.sortObject shouldBe Document("createdAt",-1)
            query.captured.limit shouldBe 21
        }
    }
})
