package net.noti_me.dymit.dymit_backend_api.application.board.dto

import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission

class BoardCommand(
    val name: String,
    val permissions: List<BoardPermission>
) {
}