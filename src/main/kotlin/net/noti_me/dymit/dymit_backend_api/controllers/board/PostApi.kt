package net.noti_me.dymit.dymit_backend_api.controllers.board

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostDetailResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostListItem

@Tag(name = "게시글 API", description = "게시글 관련 API")
@SecurityRequirement(name = "bearer-jwt")
interface PostApi {

    @Operation(method = "POST", summary = "게시글 생성", description = "특정 스터디 그룹의 게시판에 새 게시글을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "게시글이 성공적으로 생성되었습니다.")
    fun createPost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        request: PostCommandRequest
    ): PostCommandResponse

    @Operation(summary = "게시글 수정", description = "특정 스터디 그룹의 게시판에 있는 게시글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 수정되었습니다.")
    fun updatePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        request: PostCommandRequest
    ): PostCommandResponse

    @Operation(summary = "게시글 삭제", description = "특정 스터디 그룹의 게시판에 있는 게시글을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "게시글이 성공적으로 삭제되었습니다.")
    fun deletePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    )

    @Operation(summary = "게시글 목록 조회", description = "특정 스터디 그룹의 게시판에 있는 모든 게시글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록이 성공적으로 조회되었습니다.")
    fun getBoardPosts(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        cursor: String? = null,
        size: Int = 20
    ): ListResponse<PostListItem>

    @Operation(summary = "게시글 상세 조회", description = "특정 스터디 그룹의 게시판에 있는 게시글을 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 조회되었습니다.")
    fun getPost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ): PostDetailResponse
}