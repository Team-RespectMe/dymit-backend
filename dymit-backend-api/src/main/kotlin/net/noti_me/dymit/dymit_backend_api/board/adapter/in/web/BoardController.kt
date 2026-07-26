package net.noti_me.dymit.dymit_backend_api.board.adapter.`in`.web

import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.BoardService
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.BoardApi
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.BoardListItem
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.BoardResponse
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class BoardController(
    private val boardService: BoardService
): BoardApi {

    @GetMapping("/study-groups/{groupId}/boards")
    @ResponseStatus(OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getGroupBoards(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): ListResponse<BoardListItem> {
        return ListResponse.from(
            boardService.getGroupBoards(groupId).map { BoardListItem.from(it) }
        )
    }

    @GetMapping("/study-groups/{groupId}/boards/{boardId}")
    @ResponseStatus(OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getBoard(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable boardId: String
    ): BoardResponse {
        return BoardResponse.from(
            boardService.getGroupBoards(groupId).firstOrNull { it.id == boardId }
                ?: throw RuntimeException("해당 게시판을 찾을 수 없습니다.")
        )
    }
}
