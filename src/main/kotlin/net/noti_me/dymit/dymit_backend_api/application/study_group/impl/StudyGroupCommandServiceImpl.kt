package net.noti_me.dymit.dymit_backend_api.application.study_group.impl

import net.noti_me.dymit.dymit_backend_api.application.board.BoardService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupCommandService
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.ChangeGroupOwnerCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.EnlistBlacklistCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupImageUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupJoinCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupModifyCommand
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.GroupMemberJoinEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.GroupMemberLeaveEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
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

    private final val MAX_OWNED_GROUPS = 5L

    override fun createStudyGroup(member: MemberInfo,
                                  command: StudyGroupCreateCommand)
    : StudyGroupDto {
        val memberEntity = loadMemberPort.loadById(member.memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        val groupCount = loadStudyGroupPort.countByOwnerId(memberEntity.identifier)

        if ( groupCount > MAX_OWNED_GROUPS ) {
            throw BadRequestException(message = "하나의 멤버는 최대 $MAX_OWNED_GROUPS 개의 스터디 그룹만 생성할 수 있습니다.")
        }

        var studyGroup = StudyGroup(
            name = command.name,
            description = command.description,
            ownerId = memberEntity.id!!,
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
            groupId = studyGroup.id!!,
            memberId = memberEntity.id,
            nickname = memberEntity.nickname,
            profileImage = ProfileImageVo.from(memberEntity.profileImage),
            role = GroupMemberRole.OWNER
        )
        studyGroupMemberRepository.persist(owner)
        createStudyGroupBoard(member, studyGroup, "공지 사항")

        return StudyGroupDto
            .fromEntity(studyGroup)
    }

    override fun joinStudyGroup(memberInfo: MemberInfo,
                                command: StudyGroupJoinCommand)
    : StudyGroupMemberDto {
        val group = loadStudyGroupPort.loadByInviteCode(inviteCode = command.inviteCode)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")
        if ( group.isBlackListed(memberInfo.memberId) ) {
            throw ForbiddenException(message = "해당 스터디 그룹에 가입할 수 없습니다.")
        }

        val member = loadMemberPort.loadById(memberInfo.memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        if (studyGroupMemberRepository.findByGroupIdAndMemberId(
                groupId = group.id!!,
                memberId = member.id!!
        ) != null) {
            throw ConflictException(message="이미 해당 스터디 그룹에 가입되어 있습니다.")
        }

        if ( group.isBlackListed(member.id.toHexString() ) ) {
            throw ForbiddenException(message = "강제 퇴장 당한 그룹입니다.")
        }

        var newMember = StudyGroupMember(
            groupId = group.id,
            memberId = member.id,
            nickname = member.nickname,
            profileImage = ProfileImageVo.from(member.profileImage),
            role = GroupMemberRole.MEMBER,
        )
        newMember = studyGroupMemberRepository.persist(newMember)

        applicationEventPublisher.publishEvent(
            GroupMemberJoinEvent(
                group = group,
                member = newMember
            )
        )

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
        return if ( command.type == ProfileImageType.PRESET ) {
            GroupProfileImageVo(
                type = ProfileImageType.PRESET,
                thumbnail = command.value!!.thumbnail,
                original = command.value!!.original,
            )
        } else {
            throw BadRequestException(message = "지원하지 않는 이미지 타입입니다.")
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
        val memberCount = studyGroupMemberRepository.countByGroupId(group.id!!)
        if (memberCount != 1L) {
            throw BadRequestException(message = "스터디 그룹에 다른 멤버가 있어 삭제할 수 없습니다.")
        }

        // 4. 그 외에는 허용 - 스터디 그룹 삭제
        return saveStudyGroupPort.delete(group)
    }

    override fun leaveStudyGroup(
        member: MemberInfo,
        groupId: String
    ): Unit {
        val group = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        val membership = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = group.id!!,
            memberId = ObjectId(member.memberId)
        ) ?: throw NotFoundException(message = "해당 스터디 그룹의 멤버가 아닙니다.")

        if ( group.memberCount >= 2 && membership.role == GroupMemberRole.OWNER) {
            throw ConflictException(message = "스터디 그룹장은 스터디 그룹을 탈퇴할 수 없습니다. 다른 인원을 모두 탈퇴시킨 뒤 다시 시도하세요.")
        }

        applicationEventPublisher.publishEvent(GroupMemberLeaveEvent(
            group = group,
            member = membership
        ))
        studyGroupMemberRepository.delete(membership)
    }

    override fun expelStudyGroupMember(
        loginMember: MemberInfo,
        groupId: String,
        targetMemberId: String
    ) {
        val loginMembership = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = ObjectId(groupId),
            memberId = ObjectId(loginMember.memberId)
        ) ?: throw NotFoundException(message = "해당 스터디 그룹의 멤버가 아닙니다.")

        studyGroupMemberRepository.delete(loginMembership)
    }

    override fun enlistBlacklist(
        loginMember: MemberInfo,
        command: EnlistBlacklistCommand
    ) {
        val group = loadStudyGroupPort.loadByGroupId(command.groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")
        val loginMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = group.id!!,
            memberId = ObjectId(loginMember.memberId)
        ) ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")
        val targetMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = group.id!!,
            memberId = ObjectId(command.targetMember)
        ) ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        group.addBlackList(
            requester=loginMember,
            targetMember= targetMember,
            reason = command.reason
        )
        saveStudyGroupPort.update(group)
    }

    override fun delistBlacklist(loginMember: MemberInfo, groupId: String, targetMemberId: String) {
        val group = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")
        val loginMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = group.id!!,
            memberId = ObjectId(loginMember.memberId)
        ) ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        group.removeBlackList(
            requester = loginMember,
            targetMemberId = targetMemberId
        )
        saveStudyGroupPort.update(group)
    }

    override fun changeStudyGroupOwner(
        loginMember: MemberInfo,
        command: ChangeGroupOwnerCommand
    ): StudyGroupDto {
        val group = loadStudyGroupPort.loadByGroupId(command.groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        val loginMembership = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = group.id!!,
            memberId = ObjectId(loginMember.memberId)
        ) ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        val targetMembership = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = group.id!!,
            memberId = ObjectId(command.newOwnerId)
        ) ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        val groupCount = loadStudyGroupPort.countByOwnerId(command.newOwnerId)

        if ( groupCount > MAX_OWNED_GROUPS ) {
            throw BadRequestException(message = "해당 멤버는 이미 $MAX_OWNED_GROUPS  이상 그룹의 그룹장입니다.")
        }

        group.changeOwner(requester = loginMembership, newOwner = targetMembership)
        studyGroupMemberRepository.saveAll(listOf(loginMembership, targetMembership))
        saveStudyGroupPort.update(group)
        return StudyGroupDto.fromEntity(group)
    }

    override fun updateStudyGroupInfo(
        member: MemberInfo,
        command: StudyGroupModifyCommand
    ): StudyGroupDto {
        val group = loadStudyGroupPort.loadByGroupId(command.groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        group.changeName( member.memberId, command.name )
        group.changeDescription( member.memberId, command.description )
        val updatedGroup = saveStudyGroupPort.update(group)
        return StudyGroupDto.fromEntity(updatedGroup)
    }
}