package net.noti_me.dymit.dymit_backend_api.board.application.v2

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.UpdatePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.BoardStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardRecentPostDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_schedule.BoardScheduleParticipantPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 게시글 수정 유즈케이스 V2 구현체입니다.
 */
@Service
class UpdatePostUseCaseImplV2(
    private val postRepository: PostRepositoryV2,
    private val boardRepository: BoardRepositoryV2,
    private val studyGroupPort: BoardStudyGroupPort,
    private val scheduleParticipantRepository: BoardScheduleParticipantPort
) : UpdatePostUseCaseV2 {

    override fun execute(memberInfo: MemberInfo, postId: String, command: PostCommandV2): PostDtoV2 {
        val board = boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message = "해당 게시판을 찾을 수 없습니다.")
        val post = postRepository.findById(postId)
            ?: throw NotFoundException(message = "해당 게시글을 찾을 수 없습니다.")
        val group = studyGroupPort.loadGroup(command.groupId)
            ?: throw NotFoundException(message = "해당 그룹을 찾을 수 없습니다.")
        val groupMember = studyGroupPort.loadMember(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        )?.toDomain() ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

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
            studyGroupPort.updateRecentPost(
                command.groupId,
                BoardRecentPostDto(
                    postId = updatedPost.identifier,
                    title = updatedPost.title,
                    createdAt = updatedPost.createdAt ?: java.time.Instant.now()
                )
            )
        }
        return PostDtoV2.from(updatedPost)
    }
}
