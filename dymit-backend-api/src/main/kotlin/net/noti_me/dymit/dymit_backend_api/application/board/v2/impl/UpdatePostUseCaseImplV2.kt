package net.noti_me.dymit.dymit_backend_api.application.board.v2.impl

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.UpdatePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupRecentPostDto as RecentPostVo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupCommandPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleQueryPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 게시글 수정 유즈케이스 V2 구현체입니다.
 */
@Service
class UpdatePostUseCaseImplV2(
    private val postRepository: PostRepositoryV2,
    private val boardRepository: BoardRepositoryV2,
    private val loadGroupPort: StudyGroupQueryPort,
    private val saveGroupPort: StudyGroupCommandPort,
    private val groupMemberRepository: StudyGroupMemberPort,
    private val scheduleParticipantRepository: StudyScheduleQueryPort
) : UpdatePostUseCaseV2 {

    override fun update(memberInfo: MemberInfo, postId: String, command: PostCommandV2): PostDtoV2 {
        val board = boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message = "해당 게시판을 찾을 수 없습니다.")
        val post = postRepository.findById(postId)
            ?: throw NotFoundException(message = "해당 게시글을 찾을 수 없습니다.")
        val group = loadGroupPort.loadByGroupId(command.groupId)
            ?: throw NotFoundException(message = "해당 그룹을 찾을 수 없습니다.")
        val groupMember = groupMemberRepository.findByGroupIdAndMemberId(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

        if (board.groupId != ObjectId(command.groupId)) {
            throw NotFoundException(message = "해당 그룹의 게시판이 아닙니다.")
        }
        if (post.groupId != ObjectId(command.groupId) || post.boardId != ObjectId(command.boardId)) {
            throw NotFoundException(message = "해당 게시판의 게시글이 아닙니다.")
        }

        val normalizedScheduleId = PostCategoryPermissionValidatorV2.validate(
            board = board,
            groupMember = groupMember,
            category = command.category,
            scheduleId = command.scheduleId,
            scheduleParticipantRepository = scheduleParticipantRepository
        )

        post.updateTitle(memberInfo.memberId, command.title)
        post.updateContent(memberInfo.memberId, command.content)
        post.updateCategory(
            requesterId = memberInfo.memberId,
            newCategory = command.category,
            newScheduleId = normalizedScheduleId
        )
        val updatedPost = postRepository.save(post)

        if (group.recentPost?.postId == updatedPost.identifier) {
            group.updateRecentPost(
                RecentPostVo(
                    postId = updatedPost.identifier,
                    title = updatedPost.title,
                    createdAt = updatedPost.createdAt ?: java.time.LocalDateTime.now()
                )
            )
            saveGroupPort.update(group)
        }
        return PostDtoV2.from(updatedPost)
    }
}
