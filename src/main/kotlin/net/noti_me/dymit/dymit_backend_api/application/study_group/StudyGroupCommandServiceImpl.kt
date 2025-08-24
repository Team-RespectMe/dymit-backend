package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.board.BoardService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardCommand
import net.noti_me.dymit.dymit_backend_api.application.board.impl.BoardServiceImpl
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupJoinCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
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
//
//    override fun leaveStudyGroup(member: MemberInfo,
//                                 groupId: String)
//    : Boolean {
        /**
         * 1. 스터디그룹을 groupId 로 조회하고,
         * 2. 스터디그룹멤버 엔티티를 groupId와 member.memberId로 조회하고
         * 3. 스터디 그룹 멤버 엔티티를 삭제하기 전 체크
         *    - 스터디 그룹의 소유자인지?
         *      a. 소유자인 경우, 그룹 내 다른 인원이 있다면(memberCount>1) 탈퇴 불가
         *      b. 소유자가 아니라면 탈퇴 가능
         *    - 스터디 그룹 멤버 프리뷰에 등록되어있다면 스터디 그룹 엔티티도 조작
         * 4. 스터디 그룹 멤버 엔티티 삭제
         * 5. 스터디 그룹 엔티티 저장
         * note. 어떤 방식이든 락이 필요함.
         * - 그룹 소유자 변경 로직 수행 중이고, 삭제 대상 멤버가 새로운 그룹 소유자 대상이라면
         *   그룹 소유자 변경 로직에서 새로운 그룹 소유자 멤버의 존재 여부를 확인한 상태에서 변경 전
         *   해당 회원이 탈퇴해버리면 탈퇴한 회원이 그룹 소유자인 상태가 될 수 있음.
         */
//    }
}