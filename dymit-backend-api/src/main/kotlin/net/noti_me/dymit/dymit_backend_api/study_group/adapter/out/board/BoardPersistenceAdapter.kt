package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.board

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.board.StudyGroupBoardPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.board.dto.StudyGroupBoardData
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

@Component
class BoardPersistenceAdapter(
    private val mongoTemplate: MongoTemplate
) : StudyGroupBoardPort {

    override fun createDefaultBoard(
        groupId: String,
        boardName: String
    ) {
        val commonActions = listOf("READ_POST", "WRITE_COMMENT", "READ_COMMENT")
        val now = Instant.now().toMongoDate()
        val board = Document()
            .append("_id", ObjectId.get())
            .append("groupId", ObjectId(groupId))
            .append("name", boardName)
            .append(
                "permissions",
                listOf(
                    permission(
                        "OWNER",
                        commonActions + listOf(
                            "MANAGE_BOARD",
                            "WRITE_POST",
                            "DELETE_POST",
                            "DELETE_COMMENT"
                        )
                    ),
                    permission(
                        "ADMIN",
                        commonActions + listOf(
                            "WRITE_POST",
                            "DELETE_POST",
                            "DELETE_COMMENT"
                        )
                    ),
                    permission("MEMBER", commonActions)
                )
            )
            .append("categoryPolicies", defaultCategoryPolicies())
            .append("createdAt", now)
            .append("updatedAt", now)
            .append("isDeleted", false)

        mongoTemplate.insert(board, BOARD_COLLECTION)
    }

    override fun loadFirstBoard(groupId: String): StudyGroupBoardData? {
        val query = Query(Criteria.where("groupId").`is`(ObjectId(groupId)))
        return mongoTemplate.findOne(query, Document::class.java, BOARD_COLLECTION)?.let { board ->
            val id = board.getObjectId("_id") ?: return@let null
            StudyGroupBoardData(
                id = id.toHexString(),
                name = board.getString("name")
            )
        }
    }

    private fun permission(role: String, actions: List<String>) =
        Document("role", role).append("actions", actions)

    private fun defaultCategoryPolicies() = listOf(
        categoryPolicy("NOTICE", "GROUP_ADMIN_ONLY"),
        categoryPolicy("RETROSPECTIVE", "SCHEDULE_PARTICIPANT_ONLY"),
        categoryPolicy("QUESTION", "ALL_MEMBERS"),
        categoryPolicy("ASSIGNMENT", "ALL_MEMBERS")
    )

    private fun categoryPolicy(category: String, writePolicy: String) =
        Document("category", category)
            .append("enabled", true)
            .append("writePolicy", writePolicy)

    private fun Instant.toMongoDate(): Date =
        Date.from(this)

    companion object {
        private const val BOARD_COLLECTION = "study_group_boards"
    }
}
