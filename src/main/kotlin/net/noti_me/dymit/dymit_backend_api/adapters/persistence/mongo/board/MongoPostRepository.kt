package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.PostRepository
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
class MongoPostRepository(
    private val mongoTemplate: MongoTemplate
) : PostRepository {

    override fun save(post: Post): Post {
        return mongoTemplate.save(post)
    }

    override fun saveAll(posts: List<Post>): List<Post> {
        val ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Post::class.java)

        posts.forEach { post ->
            val query = Query(Criteria.where("id").`is`(post.id))
            val update = Update()
            val doc = mongoTemplate.getConverter()
                .convertToMongoType(post) as Document
            doc.forEach { key, value ->
                if (key != "_id") {
                    update.set(key, value)
                }
            }
            ops.upsert(query, update)
        }

        ops.execute()
        return posts
    }

    override fun findById(id: String): Post? {
        return try {
            val objectId = ObjectId(id)
            mongoTemplate.findById(objectId, Post::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun findByGroupId(groupId: String): List<Post> {
        return try {
            val objectId = ObjectId(groupId)
            val query = Query(Criteria.where("groupId").`is`(objectId))
            mongoTemplate.find(query, Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun findByBoardId(boardId: String): List<Post> {
        return try {
            val objectId = ObjectId(boardId)
            val query = Query(Criteria.where("boardId").`is`(objectId))
            mongoTemplate.find(query, Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun findByBoardIdLteId(boardId: String, lastId: String?, limit: Int): List<Post> {
        val objectId = ObjectId(boardId)
        val query = Query(Criteria.where("boardId").`is`(objectId))
        if (lastId != null) {
            val lastObjectId = ObjectId(lastId)
            query.addCriteria(Criteria.where("_id").lte(lastObjectId))
        }
        query.limit(limit)
        query.with(Sort.by(Sort.Direction.DESC, "_id"))
        return mongoTemplate.find(query, Post::class.java)
    }

    override fun deleteById(id: String): Boolean {
        return try {
            val objectId = ObjectId(id)
            val query = Query(Criteria.where("id").`is`(objectId))
            val result = mongoTemplate.remove(query, Post::class.java)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override fun findByWriterId(writerId: String, lastId: String?, limit: Int): List<Post> {
        val objId = ObjectId(writerId)
        val query = Query(Criteria.where("writer.id").`is`(objId))
        if (lastId != null) {
            val lastObjId = ObjectId(lastId)
            query.addCriteria(Criteria.where("_id").lte(lastObjId))
        }
        query.limit(limit)
        query.with(Sort.by(Sort.Direction.DESC, "_id"))
        return mongoTemplate.find(query, Post::class.java)
    }
}