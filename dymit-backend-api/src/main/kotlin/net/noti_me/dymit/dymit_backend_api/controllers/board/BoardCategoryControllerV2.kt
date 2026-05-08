package net.noti_me.dymit.dymit_backend_api.controllers.board

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.board.v2.BoardServiceFacadeV2
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.BoardCategoryPolicyResponseV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.UpdateBoardCategoryPoliciesRequestV2
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 게시판 카테고리 정책 V2 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v2")
class BoardCategoryControllerV2(
    private val boardServiceFacadeV2: BoardServiceFacadeV2
) : BoardCategoryApiV2 {

    @GetMapping("/study-groups/{groupId}/boards/{boardId}/categories")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getBoardCategoryPolicies(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String
    ): ListResponse<BoardCategoryPolicyResponseV2> {
        return ListResponse.from(
            boardServiceFacadeV2.getBoardCategories(memberInfo, groupId, boardId)
                .map { BoardCategoryPolicyResponseV2.from(it) }
        )
    }

    @PutMapping("/study-groups/{groupId}/boards/{boardId}/categories")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun updateBoardCategoryPolicies(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String,
        @RequestBody @Valid @Sanitize request: UpdateBoardCategoryPoliciesRequestV2
    ): ListResponse<BoardCategoryPolicyResponseV2> {
        return ListResponse.from(
            boardServiceFacadeV2.updateBoardCategories(
                memberInfo = memberInfo,
                groupId = groupId,
                boardId = boardId,
                command = request.toCommand()
            ).map { BoardCategoryPolicyResponseV2.from(it) }
        )
    }
}
