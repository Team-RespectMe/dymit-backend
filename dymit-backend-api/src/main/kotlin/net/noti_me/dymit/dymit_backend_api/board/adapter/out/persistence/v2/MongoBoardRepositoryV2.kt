package net.noti_me.dymit.dymit_backend_api.board.adapter.out.persistence.v2

import net.noti_me.dymit.dymit_backend_api.board.domain.Board
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.BoardRepositoryV2
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
