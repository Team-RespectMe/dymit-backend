package net.noti_me.dymit.dymit_backend_api.board.application.v2

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.RemovePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.BoardStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardRecentPostDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 게시글 삭제 유즈케이스 V2 구현체입니다.
 */
@Service
class RemovePostUseCaseImplV2(
    private val postRepository: PostRepositoryV2,
    private val studyGroupPort: BoardStudyGroupPort
) : RemovePostUseCaseV2 {

    override fun execute(memberInfo: MemberInfo, groupId: String, boardId: String, postId: String) {
        studyGroupPort.loadGroup(groupId)
            ?: throw NotFoundException(message = "해당 그룹을 찾을 수 없습니다.")
        val post = postRepository.findById(postId)
            ?: throw NotFoundException(message = "해당 게시글을 찾을 수 없습니다.")
        studyGroupPort.loadMember(post.groupId, ObjectId(memberInfo.memberId))
            ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

        postRepository.deleteById(post.identifier)
        val recentPost = postRepository.findLastPostByGroupIdAndBoardId(
            groupId = ObjectId(groupId),
            boardId = ObjectId(boardId)
        )
        studyGroupPort.updateRecentPost(
            groupId,
            recentPost?.let {
                BoardRecentPostDto(
                    postId = it.identifier,
                    title = it.title,
                    createdAt = it.createdAt ?: java.time.LocalDateTime.now()
                )
            }
        )
    }
}
