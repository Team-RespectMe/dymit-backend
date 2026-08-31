package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.study_group_posts

import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.PostPreview
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.LoadStudyGroupPostPort
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * MongoDB에서 스터디 그룹 공지 게시글을 조회하는 어댑터입니다.
 *
 * @property mongoTemplate MongoDB 조회 템플릿
 */
@Component
class LoadStudyGroupPostAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadStudyGroupPostPort {

    /**
     * 공지 게시판에서 삭제되지 않은 최신 게시글 한 건을 조회합니다.
     *
     * @param boardId 조회할 공지 게시판 ID
     * @return 최신 게시글 미리보기, 게시글이 없으면 null
     */
    override fun loadLatestPost(boardId: String): PostPreview? {
        if (boardId.isBlank() || !ObjectId.isValid(boardId)) {
            return null
        }

        val query = Query(
            Criteria.where("boardId").`is`(ObjectId(boardId))
                .and("isDeleted").`is`(false)
        )
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
            .limit(1)

        return mongoTemplate.findOne(query, Document::class.java, POST_COLLECTION)
            ?.toPostPreview()
    }

    private fun Document.toPostPreview(): PostPreview? {
        val postId = getObjectId("_id")?.toHexString() ?: return null
        val title = getString("title") ?: return null
        val createdAt = this["createdAt"].toLocalDateTime() ?: return null
        return PostPreview(
            postId = postId,
            title = title,
            createdAt = createdAt
        )
    }

    private fun Any?.toLocalDateTime(): LocalDateTime? =
        when (this) {
            is LocalDateTime -> this
            is Date -> LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())
            is Instant -> LocalDateTime.ofInstant(this, ZoneId.systemDefault())
            else -> null
        }

    companion object {
        private const val POST_COLLECTION = "study_group_posts"
    }
}
