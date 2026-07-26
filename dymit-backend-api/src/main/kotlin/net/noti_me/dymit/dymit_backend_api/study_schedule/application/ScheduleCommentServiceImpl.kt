package net.noti_me.dymit.dymit_backend_api.study_schedule.application

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.ScheduleCommentService
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.CreateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.ScheduleCommentDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.UpdateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleCommentWriter
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleComment
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCommentCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventScheduleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleCommentRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.StudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.StudyScheduleGroupPort
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class ScheduleCommentServiceImpl(
    private val groupPort: StudyScheduleGroupPort,
    private val scheduleCommentRepository: ScheduleCommentRepository,
    private val studyScheduleRepository: StudyScheduleRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : ScheduleCommentService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun createComment(
        memberInfo: MemberInfo,
        command: CreateScheduleCommentCommand
    ): ScheduleCommentDto {
        val group = groupPort.loadByGroupId(command.groupId.toHexString())
            ?: throw NotFoundException(message = "해당 스터디 그룹을 찾을 수 없습니다.")
        val groupMember = groupPort.findMember(
            command.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "해당 스터디 그룹의 멤버가 아닙니다.")
        val schedule = studyScheduleRepository.loadById(command.scheduleId)
            ?: throw NotFoundException(message = "해당 스케줄을 찾을 수 없습니다.")
//        val member = loadMemberPort.loadById(ObjectId(memberInfo.memberId))
//            ?: throw NotFoundException(message = "멤버 정보를 찾을 수 없습니다.")

        // 4. 댓글 생성
        val writer = ScheduleCommentWriter(
            id = ObjectId(memberInfo.memberId),
            nickname = groupMember.nickname,
            image = groupMember.profileImage
        )

        val scheduleComment = ScheduleComment(
            scheduleId = command.scheduleId,
            writer = writer,
            content = command.content
        )

        // 5. 저장
        val savedComment = scheduleCommentRepository.save(scheduleComment)

        // Event 발행
        if ( memberInfo.memberId != group.ownerId.toHexString() ) {
            val event = StudyScheduleCommentCreatedEventDto(
                group = StudyScheduleEventGroupDto(
                    id = group.identifier,
                    ownerId = group.ownerId.toHexString(),
                    name = group.name,
                    profileImageThumbnail = group.profileImage.thumbnail
                ),
                schedule = StudyScheduleEventScheduleDto(
                    id = schedule.identifier,
                    groupId = schedule.groupId.toHexString(),
                    session = schedule.session
                ),
                commentId = savedComment.identifier
            )
            eventPublisher.publishEvent(event)
        }

        return ScheduleCommentDto.from(savedComment)
    }

    override fun updateComment(
        memberInfo: MemberInfo,
        command: UpdateScheduleCommentCommand
    ): ScheduleCommentDto {
        // 1. 그룹 멤버인지 확인
        val groupMember = groupPort.findMember(
            command.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "해당 스터디 그룹의 멤버가 아닙니다.")

        // 2. 댓글 존재 확인
        val comment = scheduleCommentRepository.findById(command.commentId)
            ?: throw NotFoundException(message = "댓글을 찾을 수 없습니다.")

        comment.updateContent(groupMember, command.content)
        val updatedComment = scheduleCommentRepository.save(comment)
        return ScheduleCommentDto.from(updatedComment)
    }

    override fun deleteComment(
        memberInfo: MemberInfo,
        commentId: String
    ) {
        // 1. 댓글 존재 확인
        val comment = scheduleCommentRepository.findById(ObjectId(commentId))
            ?: throw NotFoundException(message = "댓글을 찾을 수 없습니다.")

        if (memberInfo.memberId != comment.writer.id.toHexString()) {
            throw ForbiddenException(message = "댓글 작성자만 댓글을 삭제할 수 있습니다.")
        }

        // 5. 삭제
        scheduleCommentRepository.deleteById(ObjectId(commentId))
    }

    override fun getScheduleComments(
        memberInfo: MemberInfo,
        scheduleId: String,
        cursor: String?,
        size: Int
    ): List<ScheduleCommentDto> {
        // 1. 스케줄 정보로 그룹 ID 확인
        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw NotFoundException(message = "해당 스케줄을 찾을 수 없습니다.")

        // 2. 그룹 멤버인지 확인
        groupPort.findMember(
            schedule.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "해당 스터디 그룹의 멤버가 아닙니다.")

        // 3. 댓글 목록 조회
        val cursorObjectId = cursor?.let { ObjectId(it) } ?: ObjectId()
        val comments = scheduleCommentRepository.findByScheduleId(
            ObjectId(scheduleId),
            cursorObjectId,
            size.toLong()
        )

        return comments.map { ScheduleCommentDto.from(it) }
    }
}
