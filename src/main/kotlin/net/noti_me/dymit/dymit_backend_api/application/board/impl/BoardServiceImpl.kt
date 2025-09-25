package net.noti_me.dymit.dymit_backend_api.application.board.impl

import net.noti_me.dymit.dymit_backend_api.application.board.BoardService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardCommand
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class BoardServiceImpl(
    private val boardRepository: BoardRepository,
    private val studyGroupMemberRepository: StudyGroupMemberRepository
): BoardService{

    override fun createBoard(
        memberInfo: MemberInfo,
        groupId: String,
        command: BoardCommand
    ): BoardDto {
        val groupObjectId = ObjectId(groupId)
        val memberObjectId = ObjectId(memberInfo.memberId)

        // 현재 요청 사용자가 해당 그룹의 멤버인지 확인
        val groupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId)
            ?: throw ForbiddenException("해당 그룹의 멤버가 아닙니다.")

        // 새 게시판 생성
        val newBoard = Board(
//            id = ObjectId(),
            groupId = groupObjectId,
            name = command.name,
            permissions = command.permissions.toMutableSet()
        )

        // 게시판 저장
        val savedBoard = boardRepository.save(newBoard)
            ?: throw RuntimeException("게시판 생성에 실패했습니다.")

        return BoardDto.from(savedBoard)
    }

    override fun updateBoard(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        command: BoardCommand
    ): BoardDto {
        val groupObjectId = ObjectId(groupId)
        val boardObjectId = ObjectId(boardId)
        val memberObjectId = ObjectId(memberInfo.memberId)

        // 현재 요청 사용자가 해당 그룹의 멤버인지 확인
        val groupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId)
            ?: throw ForbiddenException("해당 그룹의 멤버가 아닙니다.")

        // 기존 게시판 조회
        val existingBoard = boardRepository.findById(boardObjectId)
            ?: throw NotFoundException("해당 게시판을 찾을 수 없습니다.")

        // 그룹 일치 확인
        if (existingBoard.groupId != groupObjectId) {
            throw ForbiddenException("해당 그룹의 게시판이 아닙니다.")
        }

        // 게시판 이름 업데이트
        existingBoard.updateName(groupMember, command.name)

        // 권한 업데이트
        existingBoard.updatePermissions(groupMember, command.permissions)

        // 업데이트된 게시판 저장
        val updatedBoard = boardRepository.save(existingBoard)
            ?: throw RuntimeException("게시판 업데이트에 실패했습니다.")

        return BoardDto.from(updatedBoard)
    }

    override fun removeBoard(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String
    ) {
        val groupObjectId = ObjectId(groupId)
        val boardObjectId = ObjectId(boardId)
        val memberObjectId = ObjectId(memberInfo.memberId)

        // 현재 요청 사용자가 해당 그룹의 멤버인지 확인
        val groupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId)
            ?: throw ForbiddenException("해당 그룹의 멤버가 아닙니다.")

        // 기존 게시판 조회
        val existingBoard = boardRepository.findById(boardObjectId)
            ?: throw NotFoundException("해당 게시판을 찾을 수 없습니다.")

        // 그룹 일치 확인
        if (existingBoard.groupId != groupObjectId) {
            throw ForbiddenException("해당 그룹의 게시판이 아닙니다.")
        }

        // 게시판 삭제 권한 확인 (Board 도메인에서 권한 체크)
        // Board에 delete 권한 체크 메소드가 있다면 여기서 호출
        // existingBoard.checkDeletePermission(groupMember)

        // 게시판 삭제 수행
        val deleteResult = boardRepository.delete(existingBoard)
        if (!deleteResult) {
            throw RuntimeException("게시판 삭제에 실패했습니다.")
        }
    }

    override fun getGroupBoards(groupId: String): List<BoardDto> {
        val groupObjectId = ObjectId(groupId)

        // 해당 그룹의 모든 게시판 조회
        val boards = boardRepository.findByGroupId(groupObjectId)

        return boards.map { board ->
            BoardDto.from(board)
        }
    }
}