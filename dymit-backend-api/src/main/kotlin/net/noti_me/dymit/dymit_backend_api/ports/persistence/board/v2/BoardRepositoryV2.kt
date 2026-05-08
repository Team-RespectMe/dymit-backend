package net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2

import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import org.bson.types.ObjectId

/**
 * 게시판 V2 영속성 포트입니다.
 */
interface BoardRepositoryV2 {

    fun save(board: Board): Board

    fun findById(id: ObjectId): Board?
}
