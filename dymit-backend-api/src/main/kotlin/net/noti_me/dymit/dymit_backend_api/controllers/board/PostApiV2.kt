package net.noti_me.dymit.dymit_backend_api.controllers.board

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostCommandRequestV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostCommandResponseV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostDetailResponseV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostListItemV2
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory

@Tag(name = "게시글 API V2", description = "게시글 V2 관련 API")
@SecurityRequirement(name = "bearer-jwt")
interface PostApiV2 {

    @Operation(summary = "게시글 생성 V2")
    @ApiResponse(responseCode = "201", description = "게시글 생성 성공")
    fun createPost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        @Valid request: PostCommandRequestV2
    ): PostCommandResponseV2

    @Operation(summary = "게시글 수정 V2")
    @ApiResponse(responseCode = "200", description = "게시글 수정 성공")
    fun updatePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        @Valid request: PostCommandRequestV2
    ): PostCommandResponseV2

    @Operation(summary = "게시글 삭제 V2")
    @ApiResponse(responseCode = "204", description = "게시글 삭제 성공")
    fun deletePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    )

    @Operation(summary = "게시글 목록 조회 V2")
    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
    fun getBoardPosts(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        cursor: String?,
        size: Int,
        category: PostCategory?
    ): ListResponse<PostListItemV2>

    @Operation(summary = "게시글 상세 조회 V2")
    @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공")
    fun getPost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ): PostDetailResponseV2
}
