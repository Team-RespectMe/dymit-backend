package net.noti_me.dymit.dymit_backend_api.controllers.board

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.board.PostService
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostDetailResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.PostListItem
import org.springframework.web.bind.annotation.RestController

@RestController
class PostController(
    private val postService: PostService
) : PostApi {

    override fun createPost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        @Valid @Sanitize request: PostCommandRequest
    ): PostCommandResponse {
        return PostCommandResponse.from(
            postService.createPost(
                memberInfo,
                request.toCommand(groupId, boardId)
            )
        )
    }

    override fun updatePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String,
        @Valid @Sanitize request: PostCommandRequest
    ): PostCommandResponse {
        return PostCommandResponse.from(
            postService.updatePost(
                memberInfo,
                postId,
                request.toCommand(groupId, boardId)
            )
        )
    }

    override fun deletePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ) {
        postService.removePost(
            memberInfo = memberInfo,
            groupId = groupId,
            boardId = boardId,
            postId = postId
        )
    }

    override fun getBoardPosts(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        cursor: String?,
        size: Int
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

    override fun getPost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ): PostDetailResponse {
        return PostDetailResponse.from(postService.getPost(
            memberInfo = memberInfo,
            groupId = groupId,
            boardId = boardId,
            postId = postId
        ))
    }
}