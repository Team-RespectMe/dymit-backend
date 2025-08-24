package net.noti_me.dymit.dymit_backend_api.application.board.dto

class CommentCommand(
    val groupId: String,
    val boardId: String,
    val postId: String,
    val content: String,
) {
}