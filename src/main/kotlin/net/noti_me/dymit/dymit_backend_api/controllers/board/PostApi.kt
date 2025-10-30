package net.noti_me.dymit.dymit_backend_api.controllers.board

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostDetailResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostListItem
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "게시글 API", description = "게시글 관련 API")
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearer-jwt")
interface PostApi {

    @PostMapping("/study-groups/{groupId}/boards/{boardId}/posts")
    @Operation(summary = "게시글 생성", description = "특정 스터디 그룹의 게시판에 새 게시글을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "게시글이 성공적으로 생성되었습니다.")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @RequestBody @Valid request: PostCommandRequest
    ): PostCommandResponse

    @PutMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @Operation(summary = "게시글 수정", description = "특정 스터디 그룹의 게시판에 있는 게시글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 수정되었습니다.")
    @ResponseStatus(HttpStatus.OK)
    fun updatePost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @RequestBody @Valid request: PostCommandRequest
    ): PostCommandResponse

    @Operation(summary = "게시글 삭제", description = "특정 스터디 그룹의 게시판에 있는 게시글을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "게시글이 성공적으로 삭제되었습니다.")
    @DeleteMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String
    )

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "특정 스터디 그룹의 게시판에 있는 게시글을 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 조회되었습니다.")
    @ResponseStatus(HttpStatus.OK)
    fun getPost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String
    ): PostDetailResponse

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/posts")
    @Operation(summary = "게시글 목록 조회", description = "특정 스터디 그룹의 게시판에 있는 모든 게시글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록이 성공적으로 조회되었습니다.")
    @ResponseStatus(HttpStatus.OK)
    fun getBoardPosts(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @RequestParam cursor: String? = null,
        @RequestParam(defaultValue = "20") size: Int = 20
    ): ListResponse<PostListItem>
}