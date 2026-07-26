package net.noti_me.dymit.dymit_backend_api.board.application.v2

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.GetBoardPostsUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardAction
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.BoardStudyGroupPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 게시글 목록 조회 유즈케이스 V2 구현체입니다.
 */
@Service
class GetBoardPostsUseCaseImplV2(
    private val postRepository: PostRepositoryV2,
    private val boardRepository: BoardRepositoryV2,
    private val studyGroupPort: BoardStudyGroupPort
) : GetBoardPostsUseCaseV2 {

    override fun execute(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        cursor: String?,
        size: Int,
        category: PostCategory?
    ): List<PostDtoV2> {
        val board = boardRepository.findById(ObjectId(boardId))
            ?: throw NotFoundException(message = "해당 게시판을 찾을 수 없습니다.")
        val groupMember = studyGroupPort.loadMember(
            ObjectId(groupId),
            ObjectId(memberInfo.memberId)
        )?.toDomain() ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

        if (!board.hasPermission(groupMember, BoardAction.READ_POST)) {
            throw NotFoundException(message = "해당 게시판에 글 조회 권한이 없습니다.")
        }

        return postRepository.findByBoardIdLteId(boardId, cursor, size, category)
            .sortedByDescending { it.createdAt }
            .map { PostDtoV2.from(it) }
    }
}
