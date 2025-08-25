package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.board.BoardService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardCommand
import net.noti_me.dymit.dymit_backend_api.application.board.impl.BoardServiceImpl
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupImageUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupJoinCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.events.StudyGroupCreateEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class StudyGroupCommandServiceImpl(
    private val loadMemberPort: LoadMemberPort,
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val saveStudyGroupPort: SaveStudyGroupPort,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val boardService: BoardService,
    private val applicationEventPublisher: ApplicationEventPublisher
): StudyGroupCommandService {

    override fun createStudyGroup(member: MemberInfo,
                                  command: StudyGroupCreateCommand)
    : StudyGroupDto {
        val memberEntity = loadMemberPort.loadById(member.memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        var studyGroup = StudyGroup(
            name = command.name,
            description = command.description,
            ownerId = memberEntity.id,
        )

        var inviteCode = (1..8)
            .map { (('A'..'Z') + ('0'..'9')).random() }
            .joinToString("")

        while ( loadStudyGroupPort.existsByInviteCode(inviteCode) ) {
            inviteCode = (1..8)
                .map { (('A'..'Z') + ('0'..'9')).random() }
                .joinToString("")
        }

        studyGroup.updateInviteCode(inviteCode)
        studyGroup = saveStudyGroupPort.persist(studyGroup)

        val owner = StudyGroupMember(
            groupId = studyGroup.id,
            memberId = memberEntity.id,
            nickname = memberEntity.nickname,
            profileImage = memberEntity.profileImage ?: MemberProfileImageVo(type = "preset", url = "0"),
            role = GroupMemberRole.OWNER
        )
        studyGroupMemberRepository.persist(owner)
        applicationEventPublisher.publishEvent(StudyGroupCreateEvent(studyGroup.identifier, this))

        createStudyGroupBoard(member, studyGroup, "공지 사항")

        return StudyGroupDto
            .fromEntity(studyGroup)
    }

    override fun joinStudyGroup(memberInfo: MemberInfo,
                                command: StudyGroupJoinCommand)
    : StudyGroupMemberDto {
        val group = loadStudyGroupPort.loadByInviteCode(inviteCode = command.inviteCode)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        val member = loadMemberPort.loadById(memberInfo.memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        if (studyGroupMemberRepository.findByGroupIdAndMemberId(
                groupId = group.id,
                memberId = member.id
        ) != null) {
            throw ConflictException(message="이미 해당 스터디 그룹에 가입되어 있습니다.")
        }

        var newMember = StudyGroupMember(
            groupId = group.id,
            memberId = member.id,
            nickname = member.nickname,
            profileImage = member.profileImage,
            role = GroupMemberRole.MEMBER,
        )
        newMember = studyGroupMemberRepository.persist(newMember)

        return StudyGroupMemberDto.from(newMember)
    }

    private fun createStudyGroupBoard(
        memberInfo: MemberInfo,
        studyGroup: StudyGroup,
        boardName: String) {
        val commonActions = listOf(
            BoardAction.READ_POST,
            BoardAction.WRITE_COMMENT,
            BoardAction.READ_COMMENT,
        )
        val command = BoardCommand(
            name = boardName,
            permissions = listOf(
                BoardPermission(
                    role = GroupMemberRole.OWNER,
                    actions = (commonActions + listOf(
                        BoardAction.MANAGE_BOARD,
                        BoardAction.WRITE_POST,
                        BoardAction.DELETE_POST,
                        BoardAction.DELETE_COMMENT
                    )).toMutableList()
                ),
                BoardPermission(
                    role = GroupMemberRole.ADMIN,
                    actions = (commonActions + listOf(
                        BoardAction.WRITE_POST,
                        BoardAction.DELETE_POST,
                        BoardAction.DELETE_COMMENT
                    )).toMutableList()
                ),
                BoardPermission(
                    role = GroupMemberRole.MEMBER,
                    actions = commonActions.toMutableList()
                )
            )
        )
        boardService.createBoard(memberInfo, studyGroup.identifier, command )
    }

    private fun createImageVo(command: StudyGroupImageUpdateCommand): GroupProfileImageVo {
        return when (command.type) {
            "preset" -> {
                val presetNumber = command.value
                    ?: throw BadRequestException(message = "잘못된 Preset Value입니다.")
                if (presetNumber < 0 || presetNumber > 6) {
                    throw BadRequestException(message = "존재하지 않는 Preset Value입니다.")
                }
                GroupProfileImageVo(
                    type = "preset",
                    url = presetNumber.toString()
                )
            }
            else -> throw IllegalArgumentException("Invalid image type")
        }
    }

    override fun updateStudyGroupProfileImage(
        member: MemberInfo,
        command: StudyGroupImageUpdateCommand
    ): StudyGroupDto {
        val group = loadStudyGroupPort.loadByGroupId(command.groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        val imageVo = createImageVo(command)
        group.updateProfileImage(member.memberId, imageVo )
        val updatedGroup = saveStudyGroupPort.update(group)
        return StudyGroupDto.fromEntity(updatedGroup)
    }

//
    override fun deleteStudyGroup(
        member: MemberInfo,
        groupId: String)
    : Boolean {
        // 1. 그룹을 먼저 loadStudyGroupPort를 통해 가져온다
        val group = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        // 2. 그룹 OwnerId와 현재 사용자 memberId가 동일한지 확인
        if (group.ownerId.toHexString() != member.memberId) {
            throw ForbiddenException(message = "스터디 그룹 삭제 권한이 없습니다.")
        }

        // 3. 스터디 그룹의 전체 회원수가 1명이 아니라면 reject
        val memberCount = studyGroupMemberRepository.countByGroupId(group.id)
        if (memberCount != 1L) {
            throw BadRequestException(message = "스터디 그룹에 다른 멤버가 있어 삭제할 수 없습니다.")
        }

        // 4. 그 외에는 허용 - 스터디 그룹 삭제
        return saveStudyGroupPort.delete(group)
    }
}