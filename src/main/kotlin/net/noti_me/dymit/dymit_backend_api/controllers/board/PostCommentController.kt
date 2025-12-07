package net.noti_me.dymit.dymit_backend_api.controllers.board

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.board.CommentService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.CommentCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentListItem
import org.springframework.web.bind.annotation.*

@RestController
class PostCommentController(
    private val commentService: CommentService
): PostCommentApi {

    override fun createComment(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        @Valid @Sanitize request: CommentCommandRequest
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
        @Valid @Sanitize request: CommentCommandRequest
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
        postId: String,
        cursor: String?,
        size: Int
    ): ListResponse<CommentListItem> {
        val commentDtos = commentService.getPostComments(
            memberInfo = memberInfo,
            postId = postId,
            lastCommentId = cursor,
            size = size + 1
        )
        val commentListItems = commentDtos.map { CommentListItem.from(it) }
        return ListResponse.of(
            size = size,
            items = commentListItems,
            extractors = buildMap{
                put("cursor") { it.id }
                put("size") { size }
            }
        )
    }
}