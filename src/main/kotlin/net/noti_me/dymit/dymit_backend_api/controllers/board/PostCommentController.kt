package net.noti_me.dymit.dymit_backend_api.controllers.board

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.board.CommentService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.CommentCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentListItem
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class PostCommentController(
    private val commentService: CommentService
): PostCommentApi {

    @PostMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @RequestBody @Valid @Sanitize request: CommentCommandRequest

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

    @PutMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @PathVariable commentId: String,
        @RequestBody @Valid @Sanitize request: CommentCommandRequest
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

    @DeleteMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @PathVariable commentId: String
    ) {
        commentService.removeComment(memberInfo, commentId)
    }

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.OK)
    override fun getPostComments(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "40") size: Int

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