package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.BlacklistDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupSummaryDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.board.StudyGroupBoardPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member.LoadStudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member.dto.StudyGroupMemberData
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupPresetImage
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StudyGroupQueryServiceImpl(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val loadMemberPort: LoadStudyGroupMemberPort,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val groupBoardPort: StudyGroupBoardPort,
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

        val owner = loadMemberPort.loadById(studyGroup.ownerId.toHexString())
            ?: unknownMember(studyGroup.ownerId.toHexString())

        val membersCount = studyGroupMemberRepository.countByGroupId(studyGroup.id!!)

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
            createdAt = studyGroup.createdAt?: Instant.now(),
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
        .map{ group ->
            val owner = loadMemberPort.loadById(group.ownerId.toHexString())
                ?: unknownMember(group.ownerId.toHexString())
            StudyGroupQueryModelDto.from(group, owner)
        }.toList()

        return studyGroups
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

        val owner = loadMemberPort.loadById(studyGroup.ownerId.toHexString())
            ?: unknownMember(studyGroup.ownerId.toHexString())

        val noticeBoard = groupBoardPort.loadFirstBoard(groupId)
        return StudyGroupQueryModelDto.from(
            entity = studyGroup,
            owner = owner,
            noticeBoardId = noticeBoard?.id ?: ""
        )
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
        return inviteCode.expireAt <= Instant.now()
    }

    override fun getOwnedGroupCount(memberInfo: MemberInfo): Long {
        return loadStudyGroupPort.countByOwnerId(memberInfo.memberId)
//        return studyGroupMemberRepository.countByMemberIdAndRole(
//            memberId = ObjectId(memberInfo.memberId),
//            role = GroupMemberRole.OWNER
//        )
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

    private fun unknownMember(memberId: String) = StudyGroupMemberData(
        id = memberId,
        nickname = "Unknown",
        profileImageType = StudyGroupProfileImageType.PRESET,
        profileImageThumbnail = DEFAULT_MEMBER_THUMBNAIL,
        profileImageOriginal = DEFAULT_MEMBER_ORIGINAL,
        roles = emptyList(),
        createdAt = null
    )

    companion object {
        private const val DEFAULT_MEMBER_THUMBNAIL =
            "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/check_64x64.png"
        private const val DEFAULT_MEMBER_ORIGINAL =
            "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/check_512x512.png"
    }
}
