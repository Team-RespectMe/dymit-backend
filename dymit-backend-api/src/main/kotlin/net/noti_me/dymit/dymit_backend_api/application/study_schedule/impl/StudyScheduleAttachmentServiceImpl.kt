package net.noti_me.dymit.dymit_backend_api.application.study_schedule.impl

import net.noti_me.dymit.dymit_backend_api.application.file.FileServiceFacade
import net.noti_me.dymit.dymit_backend_api.application.file.FileUrlResolver
import net.noti_me.dymit.dymit_backend_api.application.file.dto.UpdateFileStatusCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.StudyScheduleAttachmentService
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ReplaceStudyScheduleAttachmentsCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleAttachmentDto
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFile
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleAttachment
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleAttachmentLinkQueryRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleAttachmentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 스터디 일정 첨부 파일 관리 서비스 구현체입니다.
 *
 * @param studyScheduleRepository 스터디 일정 저장소
 * @param groupMemberRepository 스터디 그룹 멤버 저장소
 * @param scheduleAttachmentRepository 스케줄 첨부 저장소
 * @param userFileRepository 사용자 파일 저장소
 * @param fileServiceFacade 파일 서비스 파사드
 * @param fileUrlResolver 파일 URL 생성기
 */
@Service
class StudyScheduleAttachmentServiceImpl(
    private val studyScheduleRepository: StudyScheduleRepository,
    private val groupMemberRepository: StudyGroupMemberRepository,
    private val scheduleAttachmentRepository: ScheduleAttachmentRepository,
    private val userFileRepository: UserFileRepository,
    private val fileServiceFacade: FileServiceFacade,
    private val fileUrlResolver: FileUrlResolver
) : StudyScheduleAttachmentService {

    override fun replaceAttachments(
        memberInfo: MemberInfo,
        command: ReplaceStudyScheduleAttachmentsCommand
    ): List<StudyScheduleAttachmentDto> {
        val schedule = loadScheduleAndValidateMember(
            memberInfo = memberInfo,
            scheduleId = command.scheduleId
        )
        val scheduleId = schedule.id!!
        val requestedFileIds = command.fileIds.distinct().map(::ObjectId)
        val currentAttachments = scheduleAttachmentRepository.findByScheduleId(scheduleId)
        val currentFileIds = currentAttachments.map { it.fileId }.toSet()
        val requestedFileIdSet = requestedFileIds.toSet()

        loadAndValidateRequestedFiles(requestedFileIds)

        val removedFileIds = currentFileIds.filter { it !in requestedFileIdSet }
        val linkedFileIds = requestedFileIdSet.filter { it !in currentFileIds }
        val attachedElsewhereFileIds = if ( removedFileIds.isEmpty() ) {
            emptySet()
        } else {
            (scheduleAttachmentRepository as? ScheduleAttachmentLinkQueryRepository)
                ?.findAttachedFileIdsExcludingSchedule(
                    fileIds = removedFileIds,
                    scheduleId = scheduleId
                )
                ?: emptySet()
        }
        val filesToDowngrade = removedFileIds.filter { it !in attachedElsewhereFileIds }

        updateFileStatuses(
            fileIds = linkedFileIds,
            status = UserFileStatus.LINKED
        )
        updateFileStatuses(
            fileIds = filesToDowngrade,
            status = UserFileStatus.UPLOADED
        )

        val finalAttachments = scheduleAttachmentRepository.replaceByScheduleId(
            scheduleId = scheduleId,
            attachments = requestedFileIds.map { fileId ->
                ScheduleAttachment(
                    scheduleId = scheduleId,
                    fileId = fileId
                )
            }
        )

        return buildAttachmentDtos(
            attachments = finalAttachments,
            requestedFiles = if ( requestedFileIds.isEmpty() ) {
                emptyList()
            } else {
                userFileRepository.findByIds(requestedFileIds)
            }
        )
    }

    override fun getAttachments(
        memberInfo: MemberInfo,
        scheduleId: String
    ): List<StudyScheduleAttachmentDto> {
        val schedule = loadScheduleAndValidateMember(
            memberInfo = memberInfo,
            scheduleId = scheduleId
        )
        val attachments = scheduleAttachmentRepository.findByScheduleId(schedule.id!!)
        val fileIds = attachments.map { it.fileId }
        val files = userFileRepository.findByIds(fileIds).associateBy { it.id!! }

        return attachments.map { attachment ->
            val userFile = files[attachment.fileId]
                ?: throw NotFoundException(message = "존재하지 않는 파일입니다.")
            StudyScheduleAttachmentDto.from(
                attachment = attachment,
                userFile = userFile,
                url = fileUrlResolver.resolve(userFile.path),
                thumbnailUrl = fileUrlResolver.resolveOrNull(userFile.thumbnailPath)
            )
        }
    }

    private fun loadScheduleAndValidateMember(
        memberInfo: MemberInfo,
        scheduleId: String
    ): StudySchedule {
        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw NotFoundException(message = "존재하지 않는 스케줄입니다.")

        groupMemberRepository.findByGroupIdAndMemberId(
            groupId = schedule.groupId,
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        return schedule
    }

    private fun loadAndValidateRequestedFiles(requestedFileIds: List<ObjectId>): List<UserFile> {
        if ( requestedFileIds.isEmpty() ) {
            return emptyList()
        }

        val requestedFiles = userFileRepository.findByIds(requestedFileIds)
        if ( requestedFiles.size != requestedFileIds.size ) {
            throw NotFoundException(message = "존재하지 않는 파일이 포함되어 있습니다.")
        }

        requestedFiles.forEach { userFile ->
            if ( userFile.status != UserFileStatus.UPLOADED && userFile.status != UserFileStatus.LINKED ) {
                throw BadRequestException(message = "업로드 완료된 파일만 첨부할 수 있습니다.")
            }
        }

        return requestedFiles
    }

    private fun buildAttachmentDtos(
        attachments: List<ScheduleAttachment>,
        requestedFiles: List<UserFile>
    ): List<StudyScheduleAttachmentDto> {
        val fileMap = requestedFiles.associateBy { it.id!! }
        return attachments.map { attachment ->
            val userFile = fileMap[attachment.fileId]
                ?: throw NotFoundException(message = "존재하지 않는 파일입니다.")
            StudyScheduleAttachmentDto.from(
                attachment = attachment,
                userFile = userFile,
                url = fileUrlResolver.resolve(userFile.path),
                thumbnailUrl = fileUrlResolver.resolveOrNull(userFile.thumbnailPath)
            )
        }
    }

    private fun updateFileStatuses(
        fileIds: List<ObjectId>,
        status: UserFileStatus
    ) {
        fileIds.forEach { fileId ->
            fileServiceFacade.updateFileStatus(
                UpdateFileStatusCommand(
                    fileId = fileId.toHexString(),
                    status = status
                )
            )
        }
    }
}
