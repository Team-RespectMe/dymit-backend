package net.noti_me.dymit.dymit_backend_api.units.member.adapter.out.daily_statistics

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.member.adapter.`out`.daily_statistics.MongoMemberDailyStatisticsAdapter
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

internal class MongoMemberDailyStatisticsAdapterTest : BehaviorSpec({
    Given("member statistics collection") {
        When("the window contains member activity") {
            Then("it requests exactly joined, withdrawn, and non-deleted visitor metrics") {
                val mongoTemplate = mockk<MongoTemplate>()
                val queries = mutableListOf<Query>()
                every { mongoTemplate.count(capture(queries), Member::class.java) } returnsMany listOf(2, 3, 5)
                val start = LocalDateTime.of(2026, 7, 28, 4, 0)
                val end = LocalDateTime.of(2026, 7, 29, 4, 0)

                val result = MongoMemberDailyStatisticsAdapter(mongoTemplate).collect(start, end)

                result.joinedCount shouldBe 2
                result.withdrawnCount shouldBe 3
                result.visitorCount shouldBe 5
                queries.size shouldBe 3
                queries[0].queryObject.containsKey("createdAt") shouldBe true
                queries[1].queryObject["isDeleted"] shouldBe true
                queries[2].queryObject["isDeleted"] shouldBe false
                queries.drop(1).forEach { it.queryObject.containsKey("lastAccessAt") shouldBe true }
                queries.forEach { it.queryObject.containsKey("updatedAt") shouldBe false }
                verify(exactly = 3) { mongoTemplate.count(any<Query>(), Member::class.java) }
            }
        }
    }
})
