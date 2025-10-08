package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.CommentRepository
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class MongoPostCommentRepository(
    private val mongoTemplate: MongoTemplate
): CommentRepository {

    override fun save(comment: PostComment): PostComment {
        return mongoTemplate.save(comment)
    }

    override fun saveAll(comments: List<PostComment>) : List<PostComment> {
        val ops = mongoTemplate.bulkOps(
            BulkOperations.BulkMode.UNORDERED,
            PostComment::class.java
        )

        comments.forEach { comment ->
            val query = Query(Criteria.where("_id").`is`(comment.id))
            val update = Update()
            val doc = mongoTemplate.getConverter()
                .convertToMongoType(comment) as Document
            doc.forEach { key, value ->
                if (key != "_id") {
                    update.set(key, value)
                }
            }
            ops.upsert(query, update)
        }

        ops.execute()
        return comments
    }

    override fun findById(id: String): PostComment? {
        val objectId = ObjectId(id)
        return mongoTemplate.findById(objectId, PostComment::class.java)
    }

    override fun findByWriterId(
        writerId: String,
        lastId: String?,
        limit: Int
    ): List<PostComment> {
        val objectId = ObjectId(writerId)
        val criteria = Criteria.where("writer.id").`is`(objectId)
        if (lastId != null) {
            val lastObjectId = ObjectId(lastId)
            criteria.andOperator(Criteria.where("_id").lte(lastObjectId))
        }
        val query = Query.query(criteria)
            .limit(limit)
            .with(Sort.by(Sort.Direction.DESC, "_id"))
        return mongoTemplate.find(query, PostComment::class.java)
    }

    override fun findByPostId(postId: String): List<PostComment> {
        val objectId = ObjectId(postId)
        val query = Query.query(Criteria.where("postId").`is`(objectId))
        return mongoTemplate.find(query, PostComment::class.java)
    }

    override fun deleteById(id: String): Boolean {
        val objectId = ObjectId(id)
        val query = Query.query(Criteria.where("_id").`is`(objectId))
        val deleteResult = mongoTemplate.remove(query, PostComment::class.java)
        return deleteResult.deletedCount > 0
    }

    override fun delete(comment: PostComment): Boolean {
        val query = Query.query(Criteria.where("_id").`is`(comment.id))
        val deleteResult = mongoTemplate.remove(query, PostComment::class.java)
        return deleteResult.deletedCount > 0
    }

    override fun findByPostIdLteId(postId: String, lastId: String?, size: Int): List<PostComment> {
        val pId = ObjectId(postId)
        val criteria = Criteria.where("postId").`is`(pId)
        if (lastId != null) {
            val lId = ObjectId(lastId)
            criteria.andOperator(Criteria.where("_id").lte(lId))
        }
        val query = Query.query(criteria)
            .limit(size)
            .with(Sort.by(Sort.Direction.DESC, "_id"))
        return mongoTemplate.find(query, PostComment::class.java)
    }
}