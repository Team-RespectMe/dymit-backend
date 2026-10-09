package net.noti_me.dymit.dymit_backend_api.units.study_group.adapter.out.statistics

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.persistence.MongoStudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationResults

internal class ManagedGroupMembershipQueryTest : BehaviorSpec({

    Given("관리 그룹 목록 저장소 조회") {
        Then("활성 OWNER와 ADMIN 관계 및 삭제되지 않은 그룹만 커서 순서와 제한으로 조회한다") {
            val mongo = mockk<MongoTemplate>()
            val aggregation = slot<Aggregation>()
            val requester = ObjectId()
            val cursor = ObjectId()
            val group = ObjectId()
            every { mongo.aggregate(capture(aggregation), "study_group_members", Document::class.java) } returns
                AggregationResults(listOf(Document("_id", group)), Document())
            MongoStudyGroupMemberRepository(mongo).findManagedGroupIds(requester, cursor, 3) shouldBe listOf(group)
            val pipeline = aggregation.captured.toPipeline(Aggregation.DEFAULT_CONTEXT)
            val match = pipeline.first()["\$match"] as Document
            match["memberId"] shouldBe requester
            (match["role"] as Document)["\$in"] shouldBe listOf(GroupMemberRole.OWNER, GroupMemberRole.ADMIN)
            (match["isDeleted"] as Document)["\$ne"] shouldBe true
            (match["groupId"] as Document)["\$gt"] shouldBe cursor
            pipeline.any { (it["\$match"] as? Document)?.containsKey("group.isDeleted") == true } shouldBe true
            pipeline[pipeline.lastIndex - 1]["\$sort"] shouldBe Document("_id", 1)
            (pipeline.last()["\$limit"] as Number).toLong() shouldBe 3L
        }
    }

    Given("활성 가입 관계가 중복 저장된 그룹의 현재 인원 조회") {
        Then("요청 그룹들을 한 번 조회하고 사용자 ID를 중복 제거한 인원으로 반환한다") {
            val mongo = mockk<MongoTemplate>()
            val aggregation = slot<Aggregation>()
            val groups = listOf(ObjectId(), ObjectId())
            every { mongo.aggregate(capture(aggregation), "study_group_members", Document::class.java) } returns
                AggregationResults(listOf(Document("_id", groups[0]).append("count", 3L)), Document())
            MongoStudyGroupMemberRepository(mongo).countDistinctActiveMembers(groups) shouldBe mapOf(groups[0] to 3L)
            val pipeline = aggregation.captured.toPipeline(Aggregation.DEFAULT_CONTEXT)
            val match = pipeline.first()["\$match"] as Document
            (match["groupId"] as Document)["\$in"] shouldBe groups
            (match["isDeleted"] as Document)["\$ne"] shouldBe true
            val distinct = (pipeline[1]["\$group"] as Document)["_id"] as Document
            distinct.keys shouldBe setOf("groupId", "memberId")
        }
    }
})
