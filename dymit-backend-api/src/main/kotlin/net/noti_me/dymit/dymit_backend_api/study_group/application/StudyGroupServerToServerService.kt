package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupCommandPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupImageDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberRoleDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupProfileImageDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupRecentPostDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupRecentScheduleDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.RecentScheduleVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class StudyGroupServerToServerService(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val saveStudyGroupPort: SaveStudyGroupPort,
    private val studyGroupMemberRepository: StudyGroupMemberRepository
) : StudyGroupQueryPort, StudyGroupCommandPort, StudyGroupMemberPort {

    override fun loadByInviteCode(inviteCode: String): StudyGroupDto? =
        loadStudyGroupPort.loadByInviteCode(inviteCode)?.toServerDto()

    override fun loadByOwnerId(ownerId: String): List<StudyGroupDto> =
        loadStudyGroupPort.loadByOwnerId(ownerId).map { it.toServerDto() }

    override fun loadByGroupId(groupId: String): StudyGroupDto? =
        loadStudyGroupPort.loadByGroupId(groupId)?.toServerDto()

    override fun loadByGroupIds(groupIds: List<String>): List<StudyGroupDto> =
        loadStudyGroupPort.loadByGroupIds(groupIds).map { it.toServerDto() }

    override fun countByOwnerId(ownerId: String): Long =
        loadStudyGroupPort.countByOwnerId(ownerId)

    override fun existsByInviteCode(inviteCode: String): Boolean =
        loadStudyGroupPort.existsByInviteCode(inviteCode)

    override fun persist(studyGroup: StudyGroupDto): StudyGroupDto =
        saveSnapshot(studyGroup, saveStudyGroupPort::persist)

    override fun update(studyGroup: StudyGroupDto): StudyGroupDto =
        saveSnapshot(studyGroup, saveStudyGroupPort::update)

    override fun delete(studyGroup: StudyGroupDto): Boolean {
        val entity = loadStudyGroupPort.loadByGroupId(studyGroup.identifier) ?: return false
        return saveStudyGroupPort.delete(entity)
    }

    override fun findByMemberId(
        memberId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyGroupMemberDto> =
        studyGroupMemberRepository.findByMemberId(memberId, cursor, limit)
            .map { it.toServerDto() }

    override fun findByGroupIdAndMemberId(
        groupId: ObjectId,
        memberId: ObjectId
    ): StudyGroupMemberDto? =
        studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)?.toServerDto()

    override fun countByGroupId(groupId: ObjectId): Long =
        studyGroupMemberRepository.countByGroupId(groupId)

    override fun findByGroupId(groupId: ObjectId): List<StudyGroupMemberDto> =
        studyGroupMemberRepository.findByGroupId(groupId).map { it.toServerDto() }

    override fun findByGroupIdsOrderByCreatedAt(
        groupIds: List<ObjectId>,
        limit: Int
    ): Map<String, List<StudyGroupMemberDto>> =
        studyGroupMemberRepository.findByGroupIdsOrderByCreatedAt(groupIds, limit)
            .mapValues { (_, members) -> members.map { it.toServerDto() } }

    override fun findGroupIdsByMemberId(memberId: ObjectId): List<String> =
        studyGroupMemberRepository.findGroupIdsByMemberId(memberId)

    override fun findByGroupIdAndMemberIdsIn(
        groupId: ObjectId,
        memberIds: List<ObjectId>
    ): List<StudyGroupMemberDto> =
        studyGroupMemberRepository.findByGroupIdAndMemberIdsIn(groupId, memberIds)
            .map { it.toServerDto() }

    override fun countByMemberIdAndRole(
        memberId: ObjectId,
        role: StudyGroupMemberRoleDto
    ): Long =
        studyGroupMemberRepository.countByMemberIdAndRole(memberId, role.toDomain())

    private fun saveSnapshot(
        snapshot: StudyGroupDto,
        save: (StudyGroup) -> StudyGroup
    ): StudyGroupDto {
        val entity = loadStudyGroupPort.loadByGroupId(snapshot.identifier)
            ?: throw IllegalArgumentException("존재하지 않는 스터디 그룹입니다.")
        entity.updateRecentPost(snapshot.recentPost?.let {
            RecentPostVo(
                postId = it.postId,
                title = it.title,
                createdAt = it.createdAt
            )
        })
        entity.updateRecentSchedule(snapshot.recentSchedule?.let {
            RecentScheduleVo(
                scheduleId = it.scheduleId,
                title = it.title,
                session = it.session,
                scheduleAt = it.scheduleAt
            )
        })
        return save(entity).toServerDto()
    }

    private fun StudyGroup.toServerDto() = StudyGroupDto(
        id = id!!,
        ownerId = ownerId,
        name = name,
        description = description,
        profileImage = StudyGroupImageDto(
            type = profileImage.type,
            original = profileImage.original,
            thumbnail = profileImage.thumbnail
        ),
        memberCount = memberCount,
        recentPost = recentPost?.let {
            StudyGroupRecentPostDto(
                postId = it.postId,
                title = it.title,
                createdAt = it.createdAt
            )
        },
        recentSchedule = recentSchedule?.let {
            StudyGroupRecentScheduleDto(
                scheduleId = it.scheduleId,
                title = it.title,
                session = it.session,
                scheduleAt = it.scheduleAt
            )
        },
        createdAt = createdAt
    )

    private fun StudyGroupMember.toServerDto() = StudyGroupMemberDto(
        id = id,
        groupId = groupId,
        memberId = memberId,
        nickname = nickname,
        profileImage = StudyGroupProfileImageDto(
            type = profileImage.type,
            url = profileImage.url
        ),
        role = StudyGroupMemberRoleDto.valueOf(role.name),
        createdAt = createdAt,
        isDeleted = isDeleted
    )

    private fun StudyGroupMemberRoleDto.toDomain() = GroupMemberRole.valueOf(name)
}
