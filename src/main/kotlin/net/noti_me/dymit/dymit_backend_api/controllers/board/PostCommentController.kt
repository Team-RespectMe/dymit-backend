package net.noti_me.dymit.dymit_backend_api.controllers.board

import net.noti_me.dymit.dymit_backend_api.application.board.CommentService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.CommentCommand
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentListItem
import org.springframework.web.bind.annotation.*

@RestController
class PostCommentController(
    private val commentService: CommentService
): CommentApi {

    override fun createComment(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        request: CommentCommandRequest
    ): CommentCommandResponse {
        val command = CommentCommand(
            groupId = groupId,
            boardId = boardId,
            postId = postId,
            content = request.content
        )

        val commentDto = commentService.createComment(memberInfo, command)
        return CommentCommandResponse.from(commentDto)
    }

    override fun updateComment(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        commentId: String,
        request: CommentCommandRequest
    ): CommentCommandResponse {
        val command = CommentCommand(
            groupId = groupId,
            boardId = boardId,
            postId = postId,
            content = request.content
        )

        val commentDto = commentService.updateComment(memberInfo, commentId, command)
        return CommentCommandResponse.from(commentDto)
    }

    override fun deleteComment(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        commentId: String
    ) {
        commentService.removeComment(memberInfo, commentId)
    }

    override fun getPostComments(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ): ListResponse<CommentListItem> {
        val commentDtos = commentService.getPostComments(memberInfo, postId)
        val commentListItems = commentDtos.map { CommentListItem.from(it) }
        return ListResponse.from(commentListItems)
    }
}