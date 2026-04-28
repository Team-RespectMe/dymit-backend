package net.noti_me.dymit.dymit_backend_api.ports.persistence.board

import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import org.bson.types.ObjectId

interface BoardRepository {

    fun save(board: Board): Board?

    fun findById(id: ObjectId): Board?

    fun findByGroupId(groupId: ObjectId): List<Board>

    fun deleteById(id: ObjectId): Boolean

    fun delete(board: Board): Boolean
}