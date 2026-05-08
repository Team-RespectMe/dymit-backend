package net.noti_me.dymit.dymit_backend_api.controllers.board

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.BoardCategoryPolicyResponseV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.UpdateBoardCategoryPoliciesRequestV2

@Tag(name = "게시판 카테고리 API V2", description = "게시판 카테고리 정책 API")
@SecurityRequirement(name = "bearer-jwt")
interface BoardCategoryApiV2 {

    @Operation(summary = "게시판 카테고리 정책 조회 V2")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    fun getBoardCategoryPolicies(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String
    ): ListResponse<BoardCategoryPolicyResponseV2>

    @Operation(summary = "게시판 카테고리 정책 수정 V2")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    fun updateBoardCategoryPolicies(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        @Valid request: UpdateBoardCategoryPoliciesRequestV2
    ): ListResponse<BoardCategoryPolicyResponseV2>
}
