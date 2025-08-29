package net.noti_me.dymit.dymit_backend_api.controllers.board

import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentListItem
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "게시글 댓글 API", description = "게시글 댓글 API")
@RequestMapping("/api/v1/")
interface CommentApi {

    @PostMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}/comments")
    @ApiResponse(responseCode = "201", description = "댓글이 성공적으로 생성되었습니다.")
    @ResponseStatus(HttpStatus.CREATED)
    fun createComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @RequestBody request: CommentCommandRequest
    ): CommentCommandResponse

    @PutMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}/comments/{commentId}")
    @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 수정되었습니다.")
    fun updateComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @PathVariable commentId: String,
        @RequestBody request: CommentCommandRequest
    ): CommentCommandResponse

    @DeleteMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", description = "댓글이 성공적으로 삭제되었습니다.")
    fun deleteComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @PathVariable commentId: String
    )

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}/comments")
    @ApiResponse(responseCode = "200", description = "댓글 목록을 성공적으로 조회했습니다.")
    fun getPostComments(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String
    ): ListResponse<CommentListItem>
}