package net.noti_me.dymit.dymit_backend_api.controllers.board

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.BoardListItem
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.BoardResponse

@Tag(name = "스터디 그룹 게시판 API", description = "스터디 그룹 게시판 관련 API")
@SecurityRequirement(name = "bearer-jwt")
interface BoardApi {

    @Operation(summary = "스터디 그룹 게시판 목록 조회", description = "스터디 그룹에 속한 게시판 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    fun getGroupBoards(memberInfo: MemberInfo, groupId: String): ListResponse<BoardListItem>


    @Operation(summary = "스터디 그룹 게시판 단건 조회", description = "스터디 그룹에 속한 게시판을 단건 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    fun getBoard(memberInfo: MemberInfo, groupId: String, boardId: String): BoardResponse
}