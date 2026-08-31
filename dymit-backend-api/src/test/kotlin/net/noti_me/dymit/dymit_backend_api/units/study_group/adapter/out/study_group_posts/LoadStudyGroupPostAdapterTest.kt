package net.noti_me.dymit.dymit_backend_api.units.study_group.adapter.out.study_group_posts

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.study_group_posts.LoadStudyGroupPostAdapter
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

internal class LoadStudyGroupPostAdapterTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val adapter = LoadStudyGroupPostAdapter(mongoTemplate)

    init {
        afterEach { clearAllMocks() }

        given("a blank or invalid board id") {
            `when`("the adapter loads the latest post") {
                then("it returns null without querying MongoDB") {
                    adapter.loadLatestPost("") shouldBe null
                    adapter.loadLatestPost("invalid-board-id") shouldBe null

                    verify(exactly = 0) { mongoTemplate.findOne(any<Query>(), Document::class.java, any()) }
                }
            }
        }

        given("a valid notice board id with no post") {
            `when`("the adapter loads the latest post") {
                then("it sends boardId, isDeleted=false, createdAt desc and limit 1 to MongoDB") {
                    val boardId = ObjectId.get().toHexString()
                    val query = slot<Query>()
                    every {
                        mongoTemplate.findOne(
                            capture(query),
                            Document::class.java,
                            "study_group_posts"
                        )
                    } returns null

                    val result = adapter.loadLatestPost(boardId)

                    result shouldBe null
                    verify(exactly = 1) {
                        mongoTemplate.findOne(any(), Document::class.java, "study_group_posts")
                    }
                    query.captured.queryObject["boardId"] shouldBe ObjectId(boardId)
                    query.captured.queryObject["isDeleted"] shouldBe false
                    query.captured.sortObject["createdAt"] shouldBe -1
                    query.captured.limit shouldBe 1
                }
            }
        }

        given("a valid notice board id with a latest post document") {
            `when`("the adapter loads the latest post") {
                then("it maps the document to post preview fields") {
                    val boardId = ObjectId.get().toHexString()
                    val postId = ObjectId.get()
                    val createdAt = LocalDateTime.of(2026, 8, 31, 9, 30)
                    val document = Document("_id", postId)
                        .append("title", "공지 제목")
                        .append(
                            "createdAt",
                            Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant())
                        )
                    every {
                        mongoTemplate.findOne(any<Query>(), Document::class.java, "study_group_posts")
                    } returns document

                    val result = adapter.loadLatestPost(boardId)

                    result?.postId shouldBe postId.toHexString()
                    result?.title shouldBe "공지 제목"
                    result?.createdAt shouldBe createdAt
                }
            }
        }
    }
}
