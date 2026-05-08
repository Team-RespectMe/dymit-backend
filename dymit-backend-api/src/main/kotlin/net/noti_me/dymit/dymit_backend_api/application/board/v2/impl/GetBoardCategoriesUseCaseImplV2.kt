package net.noti_me.dymit.dymit_backend_api.application.board.v2.impl

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.BoardCategoryPolicyDtoV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.GetBoardCategoriesUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 게시판 카테고리 정책 조회 유즈케이스 V2 구현체입니다.
 */
@Service
class GetBoardCategoriesUseCaseImplV2(
    private val boardRepository: BoardRepositoryV2,
    private val groupMemberRepository: StudyGroupMemberRepository
) : GetBoardCategoriesUseCaseV2 {

    override fun getCategories(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String
    ): List<BoardCategoryPolicyDtoV2> {
        val board = boardRepository.findById(ObjectId(boardId))
            ?: throw NotFoundException(message = "해당 게시판을 찾을 수 없습니다.")
        groupMemberRepository.findByGroupIdAndMemberId(
            ObjectId(groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

        if (board.groupId != ObjectId(groupId)) {
            throw NotFoundException(message = "해당 그룹의 게시판이 아닙니다.")
        }

        return board.categoryPolicies
            .sortedBy { it.category.name }
            .map { BoardCategoryPolicyDtoV2.from(it) }
    }
}
