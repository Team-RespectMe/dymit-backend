package net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.board.domain.Board
import org.bson.types.ObjectId

interface BoardRepository {

    fun save(board: Board): Board?

    fun findById(id: ObjectId): Board?

    fun findByGroupId(groupId: ObjectId): List<Board>

    fun deleteById(id: ObjectId): Boolean

    fun delete(board: Board): Boolean
}