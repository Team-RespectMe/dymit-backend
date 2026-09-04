package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto

import net.noti_me.dymit.dymit_backend_api.board.domain.Board
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardPermission
import java.time.LocalDate
import java.time.Instant

class BoardDto(
    val id: String,
    val groupId: String,
    val name: String,
    val createdAt: Instant,
    val permissions: MutableList<BoardPermission>
) {

    companion object {
        fun from(entity: Board): BoardDto {
            return BoardDto(
                id = entity.identifier,
                groupId = entity.groupId.toHexString(),
                name = entity.name,
                createdAt = entity.createdAt?: Instant.now(),
                permissions = entity.permissions.toMutableList()
            )
        }
    }
}
