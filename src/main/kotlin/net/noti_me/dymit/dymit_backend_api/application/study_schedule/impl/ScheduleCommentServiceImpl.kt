package net.noti_me.dymit.dymit_backend_api.application.study_schedule.impl

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.ScheduleCommentService
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.CreateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ScheduleCommentDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.UpdateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event.ScheduleCommentCreatedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleCommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class ScheduleCommentServiceImpl(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val scheduleCommentRepository: ScheduleCommentRepository,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val studyScheduleRepository: StudyScheduleRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : ScheduleCommentService {

    override fun createComment(
        memberInfo: MemberInfo,
        command: CreateScheduleCommentCommand
    ): ScheduleCommentDto {
        val group = loadStudyGroupPort.loadByGroupId(command.groupId.toHexString())
            ?: throw NotFoundException(message = "해당 스터디 그룹을 찾을 수 없습니다.")
        val groupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
            command.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "해당 스터디 그룹의 멤버가 아닙니다.")
        val schedule = studyScheduleRepository.loadById(command.scheduleId)
            ?: throw NotFoundException(message = "해당 스케줄을 찾을 수 없습니다.")
//        val member = loadMemberPort.loadById(ObjectId(memberInfo.memberId))
//            ?: throw NotFoundException(message = "멤버 정보를 찾을 수 없습니다.")

        // 4. 댓글 생성
        val writer = Writer(
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
        eventPublisher.publishEvent(ScheduleCommentCreatedEvent(
            group = group,
            schedule = schedule,
            comment = savedComment
        ))

        return ScheduleCommentDto.from(savedComment)
    }

    override fun updateComment(
        memberInfo: MemberInfo,
        command: UpdateScheduleCommentCommand
    ): ScheduleCommentDto {
        // 1. 그룹 멤버인지 확인
        val groupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
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
        studyGroupMemberRepository.findByGroupIdAndMemberId(
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
