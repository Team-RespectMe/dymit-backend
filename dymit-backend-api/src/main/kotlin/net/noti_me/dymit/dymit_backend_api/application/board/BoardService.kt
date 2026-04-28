package net.noti_me.dymit.dymit_backend_api.application.board

import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardCommand
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface BoardService {

    /**
     * 게시판 생성, command 에 입력된 정보대로 Board Entity를 생성한 뒤
     * BoardRepository 구현체를 이용하여 저장한다.
     * @param memberInfo 멤버 정보
     * @param groupId 그룹 ID
     * @param command 게시판 생성 커맨드
     * @return 생성된 게시판 정보
     */
    fun createBoard(
        memberInfo: MemberInfo,
        groupId: String,
        command: BoardCommand
    ): BoardDto

    /**
     * 게시판 수정, command 에 입력된 정보대로 Board Entity를 수정한 뒤
     * BoardRepository 구현체를 이용하여 저장한다.
     * @param memberInfo 멤버 정보
     * @param groupId 그룹 ID
     * @param boardId 게시판 ID
     * @param command 게시판 수정 커맨드
     * @return 수정된 게시판 정보
     */
    fun updateBoard(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        command: BoardCommand
    ): BoardDto

    /**
     * 게시판 삭제, BoardRepository 구현체를 이용하여 게시판을 삭제한다.
     * @param memberInfo 멤버 정보
     * @param groupId 그룹 ID
     * @param boardId 게시판 ID
     */
    fun removeBoard(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String
    )

    /**
     * 그룹의 모든 게시판 조회, BoardRepository 구현체를 이용하여 게시판 목록을 조회한다.
     * @param groupId 그룹 ID
     * @return 게시판 목록
     */
    fun getGroupBoards(
        groupId: String
    ): List<BoardDto>
}