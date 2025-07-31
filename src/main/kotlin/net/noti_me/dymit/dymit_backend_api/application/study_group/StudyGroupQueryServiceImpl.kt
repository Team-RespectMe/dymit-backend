package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.SchedulePreview
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupSummaryDto
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StudyGroupQueryServiceImpl(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val loadMemberPort: LoadMemberPort,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
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
        val studyGroupMembers: Map<String, List<MemberPreview>> = getGroupsMemberPreview(studyGroupIds)
        val studyGroupSchedules: Map<String, SchedulePreview?> = getGroupsRecentSchedule(studyGroupIds)

        return studyGroups.map { group ->
            val members = studyGroupMembers[group.identifier] ?: emptyList()
            val recentSchedule = studyGroupSchedules[group.identifier] ?: null
            val owner = members.find{ it.memberId == group.ownerId }
                ?: MemberPreview(
                    memberId = group.ownerId,
                    nickname = "Unknown",
                    role = GroupMemberRole.OWNER,
                    profileImage = MemberProfileImageVo(type = "preset", url = "0")
                )

            StudyGroupQueryModelDto(
                id = group.identifier,
                name = group.name,
                description = group.description,
                owner = owner,
                members = members,
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

    fun getGroupsMemberPreview(groupIds: List<String>): Map<String, List<MemberPreview>> {
        return studyGroupMemberRepository.findByGroupIdsOrderByCreatedAt(groupIds, DEFAULT_MEMBER_PREVIEW_SIZE)
            .mapValues { (_, members) ->
                members.map { member ->
                    MemberPreview(
                        memberId = member.memberId,
                        nickname = member.nickname,
                        role = member.role,
                        profileImage = member.profileImage
                    )
                }
            }
    }
}