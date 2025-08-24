package net.noti_me.dymit.dymit_backend_api.controllers.board

import net.noti_me.dymit.dymit_backend_api.application.board.BoardService
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.BoardListItem
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.BoardResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class BoardController(
    private val boardService: BoardService
): BoardApi {

    override fun getGroupBoards(memberInfo: MemberInfo, groupId: String): ListResponse<BoardListItem> {
        return ListResponse.from(
            boardService.getGroupBoards(groupId).map { BoardListItem.from(it) }
        )
    }

    override fun getBoard(memberInfo: MemberInfo, groupId: String, boardId: String): BoardResponse {
        return BoardResponse.from(
            boardService.getGroupBoards(groupId).firstOrNull { it.id == boardId }
                ?: throw RuntimeException("해당 게시판을 찾을 수 없습니다.")
        )
    }
}