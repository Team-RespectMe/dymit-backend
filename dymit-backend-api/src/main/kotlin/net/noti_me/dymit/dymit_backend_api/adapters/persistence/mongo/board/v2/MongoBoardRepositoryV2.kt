package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board.v2

import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

/**
 * 게시판 V2 MongoDB 구현체입니다.
 */
@Repository
class MongoBoardRepositoryV2(
    private val mongoTemplate: MongoTemplate
) : BoardRepositoryV2 {

    override fun save(board: Board): Board {
        return mongoTemplate.save(board)
    }

    override fun findById(id: ObjectId): Board? {
        return mongoTemplate.findById(id, Board::class.java)
    }
}
