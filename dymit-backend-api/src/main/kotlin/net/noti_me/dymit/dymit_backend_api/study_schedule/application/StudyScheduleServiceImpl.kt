package net.noti_me.dymit.dymit_backend_api.study_schedule.application

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.StudyScheduleService
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleCreateCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleDetailDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleParticipantDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleSummaryDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto.RoleAssignment
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.StudyScheduleGroupPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberRoleDto as GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCreatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventMemberDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventScheduleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleModifiedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipationCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.StudyScheduleRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StudyScheduleServiceImpl(
    private val groupPort: StudyScheduleGroupPort,
    private val studyScheduleRepository: StudyScheduleRepository,
    private val participantRepository: ScheduleParticipantRepository,
    private val eventPublisher: ApplicationEventPublisher
) : StudyScheduleService {

    override fun createSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        command: StudyScheduleCreateCommand
    ): StudyScheduleDto {
        val group = groupPort.loadByGroupId(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 스터디 그룹입니다.")

        val groupMember = groupPort.findMember(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        if (groupMember.role != GroupMemberRole.OWNER) {
            throw ForbiddenException(message = "스터디 그룹의 소유자만 스케줄을 생성할 수 있습니다.")
        }

        val lastSessionNumber = studyScheduleRepository.countByGroupId(ObjectId(groupId)).toInt()
        val roles = createScheduleRoles(groupId = ObjectId(groupId), roles = command.roles)

        val newStudySchedule = StudySchedule(
            groupId = ObjectId(groupId),
            title = command.title,
            description = command.description,
            scheduleAt = command.scheduleAt,
            session = lastSessionNumber + 1L,
            location = ScheduleLocation.from(command.location),
            roles = roles
        )
        val savedSchedule = studyScheduleRepository.save(newStudySchedule)

//        group.updateRecentSchedule(RecentScheduleVo(
//            scheduleId = savedSchedule.id!!,
//            title = savedSchedule.title,
//            session = savedSchedule.session,
//            scheduleAt = savedSchedule.scheduleAt
//        ))

        eventPublisher.publishEvent(
            StudyScheduleCreatedEventDto(
                group = group.toEventDto(),
                schedule = savedSchedule.toEventDto()
            )
        )
        return StudyScheduleDto.from(savedSchedule)
    }

    override fun updateSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        command: StudyScheduleUpdateCommand
    ): StudyScheduleDto {
        var schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw IllegalArgumentException("존재하지 않는 스케줄입니다.")

        if (schedule.groupId != ObjectId(groupId)) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        val group = groupPort.loadByGroupId(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 스터디 그룹입니다.")

        if (group.ownerId.toHexString() != memberInfo.memberId) {
            throw ForbiddenException(message = "스터디 그룹의 소유자만 스케줄을 수정할 수 있습니다.")
        }

        val groupMember = groupPort.findMember(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        schedule.changeScheduleAt(group = group, requester = groupMember, newScheduleAt = command.scheduleAt)
        schedule.changeTitle(requester = groupMember, newTitle = command.title)
        schedule.changeDescription(groupMember, newDescription = command.description)
        schedule.changeLocation(
            group = group,
            requester = groupMember,
            newLocation = ScheduleLocation.from(command.location)
        )
        val roles = createScheduleRoles(groupId = ObjectId(groupId), roles = command.roles)
        schedule.updateRoles(group = group, requester = groupMember, newRoles = roles)
        schedule = studyScheduleRepository.save(schedule)
        groupPort.persist(group)

        if ( schedule.isModified() ) {
            val participants = participantRepository.getByScheduleId(schedule.id!!)
                .map { it.memberId }
                .toSet()
                .toList()
            eventPublisher.publishEvent(
                StudyScheduleModifiedEventDto(
                    group = group.toEventDto(),
                    schedule = schedule.toEventDto(),
                    memberIds = participants.map(ObjectId::toHexString)
                )
            )
        }

        return StudyScheduleDto.from(schedule)
    }

    override fun removeSchedule(memberInfo: MemberInfo, groupId: String, scheduleId: String) {
        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw IllegalArgumentException("존재하지 않는 스케줄입니다.")

        if (schedule.groupId != ObjectId(groupId)) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        val group = groupPort.loadByGroupId(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 스터디 그룹입니다.")

        if (group.ownerId.toHexString() != memberInfo.memberId) {
            throw ForbiddenException(message = "스터디 그룹의 소유자만 스케줄을 삭제할 수 있습니다.")
        }

        // 스케줄이 미래의 시점이라면 레코드 자체를 삭제한다.
        if (schedule.scheduleAt.isAfter(LocalDateTime.now())) {
            studyScheduleRepository.delete(schedule)
            val event = createScheduleCanceledEvent(group, schedule)
            eventPublisher.publishEvent(event)
        } else {
            // 과거의 스케줄이라면 소프트 딜리트
            schedule.markAsDeleted()
            studyScheduleRepository.save(schedule)
        }
    }

    override fun getScheduleDetail(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): StudyScheduleDetailDto {
        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw IllegalArgumentException("존재하지 않는 스케줄입니다.")

        if (schedule.groupId != ObjectId(groupId)) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        val groupMember = groupPort.findMember(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        val participant = participantRepository.getByScheduleIdAndMemberId(
            scheduleId = schedule.id!!,
            memberId = ObjectId(memberInfo.memberId)
        )

        val scheduleDetail = StudyScheduleDetailDto.from(schedule, participant)
        scheduleDetail.participants = getParticipants(schedule)
        return scheduleDetail
    }

    override fun getGroupSchedules(memberInfo: MemberInfo, groupId: String): List<StudyScheduleSummaryDto> {
        val groupMember = groupPort.findMember(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        if (groupMember.role != GroupMemberRole.OWNER && groupMember.role != GroupMemberRole.MEMBER) {
            throw ForbiddenException(message = "스터디 그룹의 멤버만 스케줄을 조회할 수 있습니다.")
        }

        val schedules = studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(ObjectId(groupId))
        return schedules.map { schedule ->
            StudyScheduleSummaryDto.from(schedule)
        }
    }

    override fun joinSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): StudyScheduleParticipantDto {
        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw IllegalArgumentException("존재하지 않는 스케줄입니다.")

        if ( schedule.isExpired() ) {
            throw BadRequestException(message = "지난 스케줄에는 참여할 수 없습니다.")
        }

        if (schedule.groupId != ObjectId(groupId)) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        if (participantRepository.existsByScheduleIdAndMemberId(
                scheduleId = ObjectId(scheduleId),
                memberId = ObjectId(memberInfo.memberId)
            )
        ) {
            throw ConflictException(message = "이미 해당 스케줄에 참여하고 있습니다.")
        }

        val groupMember = groupPort.findMember(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        val scheduleMember = ScheduleParticipant(
            memberId = ObjectId(memberInfo.memberId),
            scheduleId = ObjectId(scheduleId),
        )

        val participant = participantRepository.save(scheduleMember)
//        if (schedule.isRoleAssigned(participant.memberId)) {
            eventPublisher.publishEvent(
                StudyScheduleParticipatedEventDto(
                    group = groupPort.loadByGroupId(groupId)!!.toEventDto(),
                    schedule = schedule.toEventDto(),
                    member = groupMember.toEventDto()
                )
            )
//        }
        schedule.increaseParticipantCount()
        studyScheduleRepository.save(schedule)

        return StudyScheduleParticipantDto.of(
            schedule = schedule,
            member = groupMember
        )
    }

    override fun leaveSchedule(memberInfo: MemberInfo, groupId: String, scheduleId: String): Unit {
        val group = groupPort.loadByGroupId(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 스터디 그룹입니다.")

        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw IllegalArgumentException("존재하지 않는 스케줄입니다.")

        if ( schedule.isExpired() ) {
            throw BadRequestException(message = "지난 스케줄에는 참여 취소할 수 없습니다.")
        }

        if (schedule.groupId != group.id) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        if (schedule.scheduleAt.isBefore(LocalDateTime.now())) {
            throw BadRequestException(message = "과거의 스케줄은 참여를 취소할 수 없습니다.")
        }

        val participant = participantRepository.getByScheduleIdAndMemberId(schedule.id!!, ObjectId(memberInfo.memberId))
            ?: throw IllegalArgumentException("해당 스케줄에 참여하지 않은 멤버입니다.")

//        if (schedule.isRoleAssigned(participant.memberId)) {
            groupPort.findMember(
                groupId = ObjectId(groupId),
                memberId = ObjectId(memberInfo.memberId)
            )?.let {
                eventPublisher.publishEvent(
                    StudyScheduleParticipationCanceledEventDto(
                        group = group.toEventDto(),
                        schedule = schedule.toEventDto(),
                        member = it.toEventDto()
                    )
                )
            }
//        }

//        studyScheduleRepository.delete(schedule)
        participantRepository.delete(participant)
        schedule.decreaseParticipantCount()
        studyScheduleRepository.save(schedule)
    }

    private fun getParticipants(schedule: StudySchedule): List<StudyScheduleParticipantDto> {
        val memberIds = participantRepository.getByScheduleId(schedule.id!!)
            .map { it.memberId }
            .toSet()
            .toList()

        val members = groupPort.findMembers(
            groupId = schedule.groupId,
            memberIds = memberIds
        )

        return members.map { member ->
            StudyScheduleParticipantDto.of(schedule, member)
        }
    }

    private fun createScheduleRoles(groupId: ObjectId, roles: List<RoleAssignment>)
            : MutableSet<ScheduleRole> {

        val memberIds = roles.map { ObjectId(it.memberId) }
            .toSet()
            .toList()

        val members = groupPort.findMembers(groupId, memberIds)
            .associate { it.memberId.toHexString() to it }
            .toMap()

        return roles.map { role ->
            val member = members[role.memberId]
                ?: throw BadRequestException(message = "존재하지 않는 멤버입니다.")
            ScheduleRole(
                memberId = member.memberId,
                nickname = member.nickname,
                image = ProfileImageVo(member.profileImage.type, member.profileImage.url),
                color = role.color,
                roles = role.roles
            )
        }.toMutableSet()
    }

    private fun createScheduleCanceledEvent(
        group: StudyGroup,
        schedule: StudySchedule
    ): StudyScheduleCanceledEventDto {
        val participants = participantRepository.getByScheduleId(schedule.id!!)
            .map { it.memberId }
            .toSet()
            .toList()

        return StudyScheduleCanceledEventDto(
            group = group.toEventDto(),
            schedule = schedule.toEventDto(),
            memberIds = participants.map(ObjectId::toHexString)
        )
    }

    private fun StudyGroup.toEventDto() = StudyScheduleEventGroupDto(
        id = identifier,
        ownerId = ownerId.toHexString(),
        name = name,
        profileImageThumbnail = profileImage.thumbnail
    )

    private fun StudySchedule.toEventDto() = StudyScheduleEventScheduleDto(
        id = identifier,
        groupId = groupId.toHexString(),
        session = session
    )

    private fun StudyGroupMember.toEventDto() = StudyScheduleEventMemberDto(
        memberId = memberId.toHexString(),
        nickname = nickname
    )
}
