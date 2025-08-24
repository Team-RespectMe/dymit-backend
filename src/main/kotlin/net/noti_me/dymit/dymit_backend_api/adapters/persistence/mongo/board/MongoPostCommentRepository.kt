package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board

import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.CommentRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoPostCommentRepository(
    private val mongoTemplate: MongoTemplate
): CommentRepository {

    override fun save(comment: PostComment): PostComment {
        return mongoTemplate.save(comment)
    }

    override fun findById(id: String): PostComment? {
        val objectId = ObjectId(id)
        return mongoTemplate.findById(objectId, PostComment::class.java)
    }

    override fun findByPostId(postId: String): List<PostComment> {
        val objectId = ObjectId(postId)
        val query = Query.query(Criteria.where("postId").`is`(objectId))
        return mongoTemplate.find(query, PostComment::class.java)
    }

    override fun deleteById(id: String): Boolean {
        val objectId = ObjectId(id)
        val query = Query.query(Criteria.where("id").`is`(objectId))
        val deleteResult = mongoTemplate.remove(query, PostComment::class.java)
        return deleteResult.deletedCount > 0
    }

    override fun delete(comment: PostComment): Boolean {
        val query = Query.query(Criteria.where("id").`is`(comment.id))
        val deleteResult = mongoTemplate.remove(query, PostComment::class.java)
        return deleteResult.deletedCount > 0
    }

}