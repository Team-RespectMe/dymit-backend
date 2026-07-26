package net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.study_group

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupCommandPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupImageDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.StudyScheduleGroupPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupImageDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberRoleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupProfileImageDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class StudyScheduleGroupAdapter(
    private val queryPort: StudyGroupQueryPort,
    private val commandPort: StudyGroupCommandPort,
    private val memberPort: StudyGroupMemberPort
) : StudyScheduleGroupPort {

    override fun loadByGroupId(groupId: String): StudyScheduleGroupDto? {
        return queryPort.loadByGroupId(groupId)?.toScheduleDto()
    }

    override fun persist(group: StudyScheduleGroupDto): StudyScheduleGroupDto {
        return commandPort.persist(group.toGroupDto()).toScheduleDto()
    }

    override fun findMember(groupId: ObjectId, memberId: ObjectId): StudyScheduleGroupMemberDto? {
        return memberPort.findByGroupIdAndMemberId(groupId, memberId)?.toScheduleDto()
    }

    override fun findMembers(
        groupId: ObjectId,
        memberIds: List<ObjectId>
    ): List<StudyScheduleGroupMemberDto> {
        return memberPort.findByGroupIdAndMemberIdsIn(groupId, memberIds).map { it.toScheduleDto() }
    }

    private fun StudyGroupDto.toScheduleDto(): StudyScheduleGroupDto {
        return StudyScheduleGroupDto(
            id = id,
            ownerId = ownerId,
            name = name,
            description = description,
            profileImage = StudyScheduleGroupImageDto.of(
                type = profileImage.type.name,
                original = profileImage.original,
                thumbnail = profileImage.thumbnail
            ),
            memberCount = memberCount,
            createdAt = createdAt
        )
    }

    private fun StudyScheduleGroupDto.toGroupDto(): StudyGroupDto {
        return StudyGroupDto(
            id = id,
            ownerId = ownerId,
            name = name,
            description = description,
            profileImage = StudyGroupImageDto.of(
                type = profileImage.type.name,
                original = profileImage.original,
                thumbnail = profileImage.thumbnail
            ),
            memberCount = memberCount,
            createdAt = createdAt
        )
    }

    private fun StudyGroupMemberDto.toScheduleDto(): StudyScheduleGroupMemberDto {
        return StudyScheduleGroupMemberDto(
            id = id,
            groupId = groupId,
            memberId = memberId,
            nickname = nickname,
            profileImage = StudyScheduleGroupProfileImageDto.of(
                type = profileImage.type.name,
                url = profileImage.url
            ),
            role = StudyScheduleGroupMemberRoleDto.valueOf(role.name),
            createdAt = createdAt,
            isDeleted = isDeleted
        )
    }
}
