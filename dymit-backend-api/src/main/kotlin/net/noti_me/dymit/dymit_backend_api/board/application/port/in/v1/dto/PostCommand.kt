package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto

class PostCommand(
    val groupId: String,
    val boardId: String,
    val title: String,
    val content: String
) {
}