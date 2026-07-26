package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.adapter.out.persistence.mongo

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.out.persistence.mongo.MongoStudyRecruitmentAdapter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitment
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

/** Mongo study recruitment adapter unit tests. */
internal class MongoStudyRecruitmentRepositoryTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val adapter = MongoStudyRecruitmentAdapter(mongoTemplate)

    init {
        Given("a cursor and requested page size") {
            val cursor = ObjectId.get()
            val query = slot<Query>()
            val recruitment = createStudyRecruitment("recruitment-id")
            every { mongoTemplate.find(capture(query), StudyRecruitment::class.java) } returns listOf(recruitment)

            When("the Mongo adapter loads recruitments") {
                val result = adapter.findByCursorOrderByIdDesc(cursor, 21)

                Then("it delegates a deleted-filtered descending cursor query and maps the result") {
                    verify(exactly = 1) { mongoTemplate.find(any<Query>(), StudyRecruitment::class.java) }
                    query.captured.limit shouldBe 21
                    query.captured.queryObject["isDeleted"] shouldBe false
                    (query.captured.queryObject["_id"] as Document)["\$lt"] shouldBe cursor
                    query.captured.sortObject["_id"] shouldBe -1
                    result.map { it.id } shouldContainExactly listOf(recruitment.identifier)
                    result.single().externalId shouldBe "external-recruitment-id"
                }
            }
        }

        Given("no cursor") {
            val query = slot<Query>()
            every { mongoTemplate.find(capture(query), StudyRecruitment::class.java) } returns emptyList()

            When("the Mongo adapter loads the first page") {
                adapter.findByCursorOrderByIdDesc(null, 20)

                Then("it omits the cursor criterion while retaining the deletion filter") {
                    query.captured.queryObject["isDeleted"] shouldBe false
                    query.captured.queryObject.containsKey("_id") shouldBe false
                }
            }
        }
    }

    private fun createStudyRecruitment(id: String): StudyRecruitment = StudyRecruitment(
        id = ObjectId.get(),
        externalId = "external-$id",
        type = "INFLEARN",
        title = "title",
        content = "content",
        url = "https://example.com/$id",
        writer = "writer"
    )
}
