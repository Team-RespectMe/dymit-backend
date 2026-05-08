package net.noti_me.dymit.dymit_backend_api.application.board.v2.impl

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.GetPostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 게시글 단건 조회 유즈케이스 V2 구현체입니다.
 */
@Service
class GetPostUseCaseImplV2(
    private val postRepository: PostRepositoryV2,
    private val boardRepository: BoardRepositoryV2,
    private val groupMemberRepository: StudyGroupMemberRepository
) : GetPostUseCaseV2 {

    override fun get(memberInfo: MemberInfo, groupId: String, boardId: String, postId: String): PostDtoV2 {
        val post = postRepository.findById(postId)
            ?: throw NotFoundException(message = "해당 게시글을 찾을 수 없습니다.")
        val board = boardRepository.findById(post.boardId)
            ?: throw NotFoundException(message = "해당 게시판을 찾을 수 없습니다.")
        val groupMember = groupMemberRepository.findByGroupIdAndMemberId(
            post.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

        if (post.groupId != ObjectId(groupId) || post.boardId != ObjectId(boardId)) {
            throw NotFoundException(message = "해당 게시판의 게시글이 아닙니다.")
        }

        if (!board.hasPermission(groupMember, BoardAction.READ_POST)) {
            throw NotFoundException(message = "해당 게시판에 글 조회 권한이 없습니다.")
        }
        return PostDtoV2.from(post)
    }
}
