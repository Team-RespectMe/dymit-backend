package net.noti_me.dymit.dymit_backend_api.controllers.board

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.BoardListItem
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.BoardResponse
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "스터디 그룹 게시판 API", description = "스터디 그룹 게시판 관련 API")
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearer-jwt")
interface BoardApi {

    @GetMapping("/study-groups/{groupId}/boards")
    @Operation(summary = "스터디 그룹 게시판 목록 조회", description = "스터디 그룹에 속한 게시판 목록을 조회합니다.")
    @ApiResponses( value = [
        ApiResponse(responseCode = "200", description = "성공"),
    ])
    @ResponseStatus(OK)
    fun getGroupBoards(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): ListResponse<BoardListItem>

    @GetMapping("/study-groups/{groupId}/boards/{boardId}")
    @Operation(summary = "스터디 그룹 게시판 단건 조회", description = "스터디 그룹에 속한 게시판을 단건 조회합니다.")
    @ApiResponses( value = [
        ApiResponse(responseCode = "200", description = "성공")
    ])
    @ResponseStatus(OK)
    fun getBoard(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String
    ): BoardResponse
}