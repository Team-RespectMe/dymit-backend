package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board

import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoBoardRepository(
    private val mongoTemplate: MongoTemplate
): BoardRepository {

    override fun save(board: Board): Board? {
        return try {
            mongoTemplate.save(board)
        } catch (e: Exception) {
            null
        }
    }

    override fun findById(id: ObjectId): Board? {
        return try {
            mongoTemplate.findById(id, Board::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun findByGroupId(groupId: ObjectId): List<Board> {
        return try {
            val query = Query(Criteria.where("groupId").`is`(groupId))
            mongoTemplate.find(query, Board::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun deleteById(id: ObjectId): Boolean {
        return try {
            val query = Query(Criteria.where("id").`is`(id))
            val result = mongoTemplate.remove(query, Board::class.java)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override fun delete(board: Board): Boolean {
        return try {
            val result = mongoTemplate.remove(board)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}