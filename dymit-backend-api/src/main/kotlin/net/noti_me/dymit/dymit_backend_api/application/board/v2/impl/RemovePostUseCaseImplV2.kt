package net.noti_me.dymit.dymit_backend_api.application.board.v2.impl

import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.RemovePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupRecentPostDto as RecentPostVo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupCommandPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 게시글 삭제 유즈케이스 V2 구현체입니다.
 */
@Service
class RemovePostUseCaseImplV2(
    private val postRepository: PostRepositoryV2,
    private val loadGroupPort: StudyGroupQueryPort,
    private val saveGroupPort: StudyGroupCommandPort,
    private val groupMemberRepository: StudyGroupMemberPort
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
        group.updateRecentPost(
            recentPost?.let {
                RecentPostVo(
                    postId = it.identifier,
                    title = it.title,
                    createdAt = it.createdAt ?: java.time.LocalDateTime.now()
                )
            }
        )
        saveGroupPort.update(group)
    }
}
