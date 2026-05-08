package net.noti_me.dymit.dymit_backend_api.controllers.board

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.board.v2.BoardServiceFacadeV2
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostCommandRequestV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostCommandResponseV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostDetailResponseV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostListItemV2
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
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
import org.springframework.web.bind.annotation.RestController

/**
 * 게시글 V2 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v2")
class PostControllerV2(
    private val boardServiceFacadeV2: BoardServiceFacadeV2
) : PostApiV2 {

    @PostMapping("/study-groups/{groupId}/boards/{boardId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun createPost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @RequestBody @Valid @Sanitize request: PostCommandRequestV2
    ): PostCommandResponseV2 {
        return PostCommandResponseV2.from(
            boardServiceFacadeV2.createPost(memberInfo, request.toCommand(groupId, boardId))
        )
    }

    @PutMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun updatePost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String,
        @RequestBody @Valid @Sanitize request: PostCommandRequestV2
    ): PostCommandResponseV2 {
        return PostCommandResponseV2.from(
            boardServiceFacadeV2.updatePost(memberInfo, postId, request.toCommand(groupId, boardId))
        )
    }

    @DeleteMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun deletePost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String
    ) {
        boardServiceFacadeV2.removePost(
            memberInfo = memberInfo,
            groupId = groupId,
            boardId = boardId,
            postId = postId
        )
    }

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/posts")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getBoardPosts(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) category: PostCategory?
    ): ListResponse<PostListItemV2> {
        val items = boardServiceFacadeV2.getBoardPosts(
            memberInfo = memberInfo,
            groupId = groupId,
            boardId = boardId,
            cursor = cursor,
            size = size + 1,
            category = category
        ).map { PostListItemV2.from(it) }

        return ListResponse.of(
            size = size,
            items = items,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { size }
                if (category != null) {
                    put("category") { _ -> category.name }
                }
            }
        )
    }

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/posts/{postId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getPost(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @PathVariable postId: String
    ): PostDetailResponseV2 {
        return PostDetailResponseV2.from(
            boardServiceFacadeV2.getPost(
                memberInfo = memberInfo,
                groupId = groupId,
                boardId = boardId,
                postId = postId
            )
        )
    }
}
