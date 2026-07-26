package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto

class CommentCommand(
    val groupId: String,
    val boardId: String,
    val postId: String,
    val content: String,
) {
}