package net.noti_me.dymit.dymit_backend_api.units.study_group.adapter.out.board

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.board.BoardPersistenceAdapter
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

internal class BoardPersistenceAdapterTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val adapter = BoardPersistenceAdapter(mongoTemplate)

    init {
        given("a group board stored in MongoDB") {
            `when`("the adapter loads the first board") {
                then("it queries by ObjectId and maps the Mongo document to the study-group port DTO") {
                    val groupId = ObjectId.get()
                    val boardId = ObjectId.get()
                    val query = slot<Query>()
                    every {
                        mongoTemplate.findOne(capture(query), Document::class.java, "study_group_boards")
                    } returns Document("_id", boardId).append("name", "공지사항")

                    val result = adapter.loadFirstBoard(groupId.toHexString())

                    verify(exactly = 1) {
                        mongoTemplate.findOne(any(), Document::class.java, "study_group_boards")
                    }
                    query.captured.queryObject["groupId"] shouldBe groupId
                    result!!.id shouldBe boardId.toHexString()
                    result.name shouldBe "공지사항"
                }
            }
        }

        given("a default-board creation request") {
            `when`("the adapter writes the board document") {
                then("it persists the expected study-group board schema and default permissions") {
                    val groupId = ObjectId.get()
                    val board = slot<Document>()
                    every { mongoTemplate.insert(capture(board), "study_group_boards") } returns mockk()

                    adapter.createDefaultBoard(groupId.toHexString(), "기본 게시판")

                    verify(exactly = 1) { mongoTemplate.insert(any<Document>(), "study_group_boards") }
                    board.captured.getObjectId("_id") shouldNotBe null
                    board.captured.getObjectId("groupId") shouldBe groupId
                    board.captured.getString("name") shouldBe "기본 게시판"
                    board.captured["isDeleted"] shouldBe false
                    val permissions = board.captured.getList("permissions", Document::class.java)
                    permissions.map { it.getString("role") } shouldBe listOf("OWNER", "ADMIN", "MEMBER")
                }
            }
        }
    }
}
