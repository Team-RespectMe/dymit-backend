package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.BlacklistDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupSummaryDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StudyGroupQueryServiceImpl(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val loadMemberPort: LoadMemberPort,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val saveStudyGroupPort: SaveStudyGroupPort,
): StudyGroupQueryService {

    private val logger = LoggerFactory.getLogger(javaClass)

    private final val DEFAULT_MEMBER_PREVIEW_SIZE = 8

    override fun getStudyGroupByInviteCode(
        memberInfo: MemberInfo,
        inviteCode: String
    ): StudyGroupSummaryDto {

        val studyGroup = loadStudyGroupPort.loadByInviteCode(inviteCode)
            ?: throw NotFoundException(message = "해당 초대 코드를 사용하는 스터디 그룹이 존재하지 않습니다.")

        val owner = loadMemberPort.loadById(studyGroup.ownerId)
            ?: Member(
                id = studyGroup.ownerId,
                nickname = "Unknown",
                profileImage = MemberProfileImageVo(type = "preset", url = "0")
            )

        val membersCount = studyGroupMemberRepository.countByGroupId(studyGroup.id)

        val studyGroupDto =  StudyGroupSummaryDto(
            id = studyGroup.identifier,
            name = studyGroup.name,
            owner = MemberPreview.of(owner, GroupMemberRole.OWNER),
            inviteCode = InviteCodeVo(
                code = studyGroup.inviteCode.code,
                createdAt = studyGroup.inviteCode.createdAt,
                expireAt = studyGroup.inviteCode.expireAt
            ),
            description = studyGroup.description,
            membersCount = membersCount,
            createdAt = studyGroup.createdAt?: LocalDateTime.now(),
        )

        return studyGroupDto
    }

    override fun getMyStudyGroups(memberInfo: MemberInfo)
    : List<StudyGroupQueryModelDto> {
        val studyGroupIds = studyGroupMemberRepository.findGroupIdsByMemberId(
            ObjectId(memberInfo.memberId))

        if (studyGroupIds.isEmpty()) {
            return emptyList()
        }

        val studyGroups = loadStudyGroupPort.loadByGroupIds(studyGroupIds)
        // TODO : 스터디 그룹의 가장 최근 일정이 변경되어야 하는 경우가 존재함.
        //        스터디 스케줄에 대한 CUD 작업이 발생하는 경우 업데이트가 되지만,
        //        그렇지 않고 스터디 그룹의 가장 최근 일정이 변경되어야 하는 경우가 있음.
        //        예를 들어, 오늘 스터디 그룹의 가장 최근 일정이 수행된 경우, 다음 스터디 일정으로
        //        넘어 가야한다. (백그라운드 태스크를 이용할지 결정해야함)
        return studyGroups.map { group ->
            val owner = loadMemberPort.loadById(group.ownerId) ?: Member(
                id = group.ownerId,
                nickname = "Unknown",
                profileImage = MemberProfileImageVo(type = "preset", url = "0")
            )

            StudyGroupQueryModelDto.from(group, owner)
        }.toList()
    }

    override fun getInviteCode(memberInfo: MemberInfo, groupId: String): InviteCodeVo {
        var studyGroup = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        val studyGroupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "해당 스터디 그룹에 가입되어 있지 않습니다.")

        if (isExpiredInviteCode(studyGroup.inviteCode)) {
            var inviteCode = (1..8)
                .map { (('A'..'Z') + ('0'..'9')).random() }
                .joinToString("")

            while ( loadStudyGroupPort.existsByInviteCode(inviteCode) ) {
                inviteCode = (1..8)
                    .map { (('A'..'Z') + ('0'..'9')).random() }
                    .joinToString("")
            }

            studyGroup.updateInviteCode(inviteCode)
            studyGroup = saveStudyGroupPort.update(studyGroup)
        }

        return studyGroup.inviteCode
    }

    override fun getStudyGroup(memberInfo: MemberInfo, groupId: String): StudyGroupQueryModelDto {
        val studyGroup = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        val groupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "해당 스터디 그룹에 가입되어 있지 않습니다.")

        val owner = loadMemberPort.loadById(studyGroup.ownerId)
            ?: Member(
                id = studyGroup.ownerId,
                nickname = "Unknown",
                profileImage = MemberProfileImageVo(type = "preset", url = "0")
            )

        return StudyGroupQueryModelDto.from(studyGroup, owner)
    }

    override fun getStudyGroupMembers(memberInfo: MemberInfo, groupId: String): List<StudyGroupMemberQueryDto> {
        val group = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")
        val members = studyGroupMemberRepository.findByGroupId(ObjectId(groupId))
        val loginMember = members.find { it -> it.memberId == ObjectId(memberInfo.memberId) }
            ?: throw ForbiddenException(message = "해당 스터디 그룹에 가입되어 있지 않습니다.")
        return members.map { StudyGroupMemberQueryDto.from(it) }
    }

    private fun isExpiredInviteCode(inviteCode: InviteCodeVo): Boolean {
        return inviteCode.expireAt <= LocalDateTime.now()
    }

    override fun getOwnedGroupCount(memberInfo: MemberInfo): Long {
        return studyGroupMemberRepository.countByOwnerId(ObjectId(memberInfo.memberId))
    }

    override fun getBlacklists(
        memberInfo: MemberInfo,
        groupId: String
    ): List<BlacklistDto> {
        val membership = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "해당 스터디 그룹에 가입되어 있지 않습니다.")

        if (membership.role == GroupMemberRole.MEMBER) {
            throw ForbiddenException(message = "해당 스터디 그룹의 소유자 또는 관리자만 차단 목록을 조회할 수 있습니다.")
        }

        val group = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")
        return group.getBlacklisted()
            .asSequence()
            .map{ BlacklistDto.from(it) }
            .toList()
    }
}