package net.noti_me.dymit.dymit_backend_api.board.application.v1

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.BoardService
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.BoardCommand
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.BoardDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.InternalServerError
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.board.domain.Board
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.BoardRepository
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.BoardStudyGroupPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class BoardServiceImpl(
    private val boardRepository: BoardRepository,
    private val studyGroupPort: BoardStudyGroupPort
): BoardService{

    override fun createBoard(
        memberInfo: MemberInfo,
        groupId: String,
        command: BoardCommand
    ): BoardDto {
        val groupObjectId = ObjectId(groupId)
        val memberObjectId = ObjectId(memberInfo.memberId)

        // 현재 요청 사용자가 해당 그룹의 멤버인지 확인
        studyGroupPort.loadMember(groupObjectId, memberObjectId)
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
        val groupMember = studyGroupPort.loadMember(groupObjectId, memberObjectId)?.toDomain()
            ?: throw ForbiddenException("해당 그룹의 멤버가 아닙니다.")

        // 기존 게시판 조회
        val existingBoard = boardRepository.findById(boardObjectId)
            ?: throw NotFoundException("해당 게시판을 찾을 수 없습니다.")

        existingBoard.updateName(groupMember, command.name)
        existingBoard.updatePermissions(groupMember, command.permissions)
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
        studyGroupPort.loadMember(groupObjectId, memberObjectId)
            ?: throw ForbiddenException("해당 그룹의 멤버가 아닙니다.")

        // 기존 게시판 조회
        val existingBoard = boardRepository.findById(boardObjectId)
            ?: throw NotFoundException("해당 게시판을 찾을 수 없습니다.")

        // 게시판 삭제 수행
        val deleteResult = boardRepository.delete(existingBoard)
        if (!deleteResult) {
            throw InternalServerError(message="게시판 삭제에 실패했습니다.")
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
