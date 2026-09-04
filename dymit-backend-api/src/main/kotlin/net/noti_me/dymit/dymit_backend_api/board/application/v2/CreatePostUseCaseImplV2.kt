package net.noti_me.dymit.dymit_backend_api.board.application.v2

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.CreatePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.board.domain.Post
import net.noti_me.dymit.dymit_backend_api.board.domain.Writer
import net.noti_me.dymit.dymit_backend_api.board.application.event.PostCreatedEvent
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.BoardStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardRecentPostDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_schedule.BoardScheduleParticipantPort
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 게시글 생성 유즈케이스 V2 구현체입니다.
 */
@Service
class CreatePostUseCaseImplV2(
    private val postRepository: PostRepositoryV2,
    private val boardRepository: BoardRepositoryV2,
    private val studyGroupPort: BoardStudyGroupPort,
    private val scheduleParticipantRepository: BoardScheduleParticipantPort,
    private val eventPublisher: ApplicationEventPublisher
) : CreatePostUseCaseV2 {

    override fun execute(memberInfo: MemberInfo, command: PostCommandV2): PostDtoV2 {
        val board = boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message = "해당 게시판을 찾을 수 없습니다.")
        val group = studyGroupPort.loadGroup(command.groupId)
            ?: throw NotFoundException(message = "해당 그룹을 찾을 수 없습니다.")
        val groupMember = studyGroupPort.loadMember(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        )?.toDomain() ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

        if (board.groupId != ObjectId(command.groupId)) {
            throw NotFoundException(message = "해당 그룹의 게시판이 아닙니다.")
        }

        val normalizedScheduleId = PostCategoryPermissionValidatorV2.validate(
            board = board,
            groupMember = groupMember,
            category = command.category,
            scheduleId = command.scheduleId,
            scheduleParticipantRepository = scheduleParticipantRepository,
            forceRetrospectiveParticipantCheck = true
        )

        val newPost = Post(
            groupId = ObjectId(command.groupId),
            boardId = board.id!!,
            writer = Writer.of(
                id = groupMember.memberId,
                nickname = groupMember.nickname,
                imageType = groupMember.profileImage.type,
                imageUrl = groupMember.profileImage.url
            ),
            title = command.title,
            content = command.content,
            category = command.category,
            scheduleId = normalizedScheduleId
        )
        val savedPost = postRepository.save(newPost)

        studyGroupPort.updateRecentPost(
            command.groupId,
            BoardRecentPostDto(
                postId = savedPost.identifier,
                title = savedPost.title,
                createdAt = savedPost.createdAt ?: java.time.Instant.now()
            )
        )

        val event = PostCreatedEvent(
            group = group,
            board = board,
            post = savedPost
        )
        event.addExcludedMemberId(groupMember.memberId)
        eventPublisher.publishEvent(event)
        return PostDtoV2.from(savedPost)
    }
}
