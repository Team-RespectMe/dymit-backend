package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board.v2

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * 게시글 V2 MongoDB 구현체입니다.
 */
@Repository
class MongoPostRepositoryV2(
    private val mongoTemplate: MongoTemplate
) : PostRepositoryV2 {

    override fun save(post: Post): Post {
        return mongoTemplate.save(post)
    }

    override fun findById(id: String): Post? {
        return try {
            mongoTemplate.findById(ObjectId(id), Post::class.java)
        } catch (_: Exception) {
            null
        }
    }

    override fun findByBoardIdLteId(
        boardId: String,
        lastId: String?,
        limit: Int,
        category: PostCategory?
    ): List<Post> {
        val query = Query(Criteria.where("boardId").`is`(ObjectId(boardId)))
        if (lastId != null) {
            query.addCriteria(Criteria.where("_id").lt(ObjectId(lastId)))
        }
        if (category != null) {
            query.addCriteria(Criteria.where("category").`is`(category))
        }
        query.limit(limit)
        query.with(Sort.by(Sort.Direction.DESC, "_id"))
        return mongoTemplate.find(query, Post::class.java)
    }

    override fun findLastPostByGroupIdAndBoardId(groupId: ObjectId, boardId: ObjectId): Post? {
        val query = Query(
            Criteria.where("groupId").`is`(groupId)
                .and("boardId").`is`(boardId)
        )
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"))
        query.limit(1)
        return mongoTemplate.findOne(query, Post::class.java)
    }

    override fun deleteById(id: String): Boolean {
        return try {
            val query = Query(Criteria.where("_id").`is`(ObjectId(id)))
            mongoTemplate.remove(query, Post::class.java).deletedCount > 0
        } catch (_: Exception) {
            false
        }
    }
}
