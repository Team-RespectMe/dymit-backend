package net.noti_me.dymit.dymit_backend_api.controllers.board

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.CommentListItem
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "게시글 댓글 API", description = "게시글 댓글 API")
@RequestMapping("/api/v1/")
@SecurityRequirement(name = "bearer-jwt")
interface PostCommentApi {

    @ApiResponse(responseCode = "201", description = "댓글이 성공적으로 생성되었습니다.")
    @Operation(method = "POST", summary = "댓글 생성", description = "게시글에 댓글을 생성합니다.")
    fun createComment(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        request: CommentCommandRequest
    ): CommentCommandResponse


    @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 수정되었습니다.")
    @Operation(method = "PUT", summary = "댓글 수정", description = "게시글의 댓글을 수정합니다.")
    fun updateComment(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        commentId: String,
        request: CommentCommandRequest
    ): CommentCommandResponse

    @ApiResponse(responseCode = "204", description = "댓글이 성공적으로 삭제되었습니다.")
    @Operation(method = "DELETE", summary = "댓글 삭제", description = "게시글의 댓글을 삭제합니다.")
    fun deleteComment(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        commentId: String
    )

    @ApiResponse(responseCode = "200", description = "댓글 목록을 성공적으로 조회했습니다.")
    @Operation(method = "GET", summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다.")
    fun getPostComments(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        cursor: String? = null,
        size: Int = 40
    ): ListResponse<CommentListItem>
}