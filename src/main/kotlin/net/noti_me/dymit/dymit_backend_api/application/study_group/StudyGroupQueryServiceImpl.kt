package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.SchedulePreview
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupSummaryDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StudyGroupQueryServiceImpl(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val loadMemberPort: LoadMemberPort,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val saveStudyGroupPort: SaveStudyGroupPort,
//    private val studyGroupScheduleRepository: StudyGroupScheduleRepository
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

        val membersCount = studyGroupMemberRepository.countByGroupId(studyGroup.identifier)

        val studyGroupDto =  StudyGroupSummaryDto(
            id = studyGroup.identifier,
            name = studyGroup.name,
            owner = MemberPreview(
                memberId = owner.identifier,
                nickname = owner.nickname,
                role = GroupMemberRole.OWNER,
                profileImage = owner.profileImage
            ),
            description = studyGroup.description,
            membersCount = membersCount,
            createdAt = studyGroup.createdAt?: LocalDateTime.now(),
        )

        return studyGroupDto
    }

    override fun getMyStudyGroups(memberInfo: MemberInfo)
    : List<StudyGroupQueryModelDto> {
        val studyGroupIds = studyGroupMemberRepository.findGroupIdsByMemberId(memberInfo.memberId)

        if (studyGroupIds.isEmpty()) {
            return emptyList()
        }

        val studyGroups = loadStudyGroupPort.loadByGroupIds(studyGroupIds)
        val studyGroupSchedules: Map<String, SchedulePreview?> = getGroupsRecentSchedule(studyGroupIds)

        return studyGroups.map { group ->
            val recentSchedule = studyGroupSchedules[group.identifier] ?: null
            val owner = loadMemberPort.loadById(group.ownerId) ?: Member(
                id = group.ownerId,
                nickname = "Unknown",
                profileImage = MemberProfileImageVo(type = "preset", url = "0")
            )

            StudyGroupQueryModelDto(
                id = group.identifier,
                name = group.name,
                description = group.description,
                owner = MemberPreview(
                    memberId = owner.identifier,
                    nickname = owner.nickname,
                    role = GroupMemberRole.OWNER,
                    profileImage = owner.profileImage
                ),
                schedule = recentSchedule,
                createdAt = group.createdAt ?: LocalDateTime.now(),
                profileImage = group.profileImage
            )
        }.toList()
    }

    fun getGroupsRecentSchedule( groupIds: List<String> )
    : Map<String, SchedulePreview?> {
        return emptyMap()
    }

    override fun getInviteCode(memberInfo: MemberInfo, groupId: String): InviteCodeVo {
        var studyGroup = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        val studyGroupMember = studyGroupMemberRepository.findByGroupIdAndMemberId(
            groupId, memberInfo.memberId
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
            studyGroup = saveStudyGroupPort.persist(studyGroup)
        }

        return studyGroup.inviteCode
    }

    private fun isExpiredInviteCode(inviteCode: InviteCodeVo): Boolean {
        return inviteCode.expireAt <= LocalDateTime.now()
    }
}