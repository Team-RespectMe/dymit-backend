package net.noti_me.dymit.dymit_backend_api.controllers.board

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.board.PostService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostDetailResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostListItem
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class PostController(
    private val postService: PostService
) : PostApi {

    @PostMapping("/study-groups/{groupId}/boards/{boardId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createPost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @RequestBody @Valid @Sanitize request: PostCommandRequest
    ): PostCommandResponse {
        return PostCommandResponse.from(
            postService.createPost(
                memberInfo,
                request.toCommand(groupId, boardId)
            )
        )
    }

    @PutMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updatePost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @RequestBody @Valid @Sanitize request: PostCommandRequest
    ): PostCommandResponse {
        return PostCommandResponse.from(
            postService.updatePost(
                memberInfo,
                postId,
                request.toCommand(groupId, boardId)
            )
        )
    }

    @DeleteMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deletePost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String
    ) {
        postService.removePost(
            memberInfo = memberInfo,
            groupId = groupId,
            boardId = boardId,
            postId = postId
        )
    }

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/posts")
    @ResponseStatus(HttpStatus.OK)
    override fun getBoardPosts(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @RequestParam cursor: String?,
        @RequestParam(defaultValue = "20") size: Int
    ): ListResponse<PostListItem> {
        val postDtos = postService.getBoardPostsWithCursor(
            memberInfo = memberInfo,
            groupId = groupId,
            boardId = boardId,
            cursor = cursor,
            size = size + 1
        ).map {
            PostListItem.from(it)
        }

        return ListResponse.of(
            size = size,
            items = postDtos,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { size }
            }
        )
    }

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @ResponseStatus(HttpStatus.OK)
    override fun getPost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String
    ): PostDetailResponse {
        return PostDetailResponse.from(postService.getPost(
            memberInfo = memberInfo,
            groupId = groupId,
            boardId = boardId,
            postId = postId
        ))
    }
}