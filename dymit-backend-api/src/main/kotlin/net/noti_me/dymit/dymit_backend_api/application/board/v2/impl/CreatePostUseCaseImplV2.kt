package net.noti_me.dymit.dymit_backend_api.application.board.v2.impl

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.CreatePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.board.event.PostCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
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
    private val loadGroupPort: LoadStudyGroupPort,
    private val saveGroupPort: SaveStudyGroupPort,
    private val groupMemberRepository: StudyGroupMemberRepository,
    private val scheduleParticipantRepository: ScheduleParticipantRepository,
    private val eventPublisher: ApplicationEventPublisher
) : CreatePostUseCaseV2 {

    override fun create(memberInfo: MemberInfo, command: PostCommandV2): PostDtoV2 {
        val board = boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message = "해당 게시판을 찾을 수 없습니다.")
        val group = loadGroupPort.loadByGroupId(command.groupId)
            ?: throw NotFoundException(message = "해당 그룹을 찾을 수 없습니다.")
        val groupMember = groupMemberRepository.findByGroupIdAndMemberId(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")

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
            writer = Writer.from(groupMember),
            title = command.title,
            content = command.content,
            category = command.category,
            scheduleId = normalizedScheduleId
        )
        val savedPost = postRepository.save(newPost)

        group.updateRecentPost(RecentPostVo.from(savedPost))
        saveGroupPort.update(group)

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
