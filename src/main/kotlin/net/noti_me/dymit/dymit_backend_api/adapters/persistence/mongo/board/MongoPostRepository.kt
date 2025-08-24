package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.PostRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoPostRepository(
    private val mongoTemplate: MongoTemplate
) : PostRepository {

    override fun save(post: Post): Post? {
        return try {
            mongoTemplate.save(post)
        } catch (e: Exception) {
            null
        }
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
}