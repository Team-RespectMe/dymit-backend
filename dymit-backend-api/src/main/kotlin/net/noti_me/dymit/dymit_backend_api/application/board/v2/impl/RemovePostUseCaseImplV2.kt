package net.noti_me.dymit.dymit_backend_api.application.board.v2.impl

import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.RemovePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 게시글 삭제 유즈케이스 V2 구현체입니다.
 */
@Service
class RemovePostUseCaseImplV2(
    private val postRepository: PostRepositoryV2,
    private val loadGroupPort: LoadStudyGroupPort,
    private val saveGroupPort: SaveStudyGroupPort,
    private val groupMemberRepository: StudyGroupMemberRepository
) : RemovePostUseCaseV2 {

    override fun remove(memberInfo: MemberInfo, groupId: String, boardId: String, postId: String) {
        val group = loadGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "해당 그룹을 찾을 수 없습니다.")
        val post = postRepository.findById(postId)
            ?: throw NotFoundException(message = "해당 게시글을 찾을 수 없습니다.")
        groupMemberRepository.findByGroupIdAndMemberId(post.groupId, ObjectId(memberInfo.memberId))
            ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

        postRepository.deleteById(post.identifier)
        val recentPost = postRepository.findLastPostByGroupIdAndBoardId(
            groupId = ObjectId(groupId),
            boardId = ObjectId(boardId)
        )
        group.updateRecentPost(RecentPostVo.from(recentPost))
        saveGroupPort.update(group)
    }
}
