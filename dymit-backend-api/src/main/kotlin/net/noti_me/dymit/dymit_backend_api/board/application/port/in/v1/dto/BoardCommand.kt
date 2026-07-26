package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto

import net.noti_me.dymit.dymit_backend_api.board.domain.BoardPermission

class BoardCommand(
    val name: String,
    val permissions: List<BoardPermission>
) {
}