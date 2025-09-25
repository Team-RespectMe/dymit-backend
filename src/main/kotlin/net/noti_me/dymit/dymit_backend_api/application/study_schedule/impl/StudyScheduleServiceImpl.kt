package net.noti_me.dymit.dymit_backend_api.application.study_schedule.impl

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.StudyScheduleService
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleDetailDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleParticipantDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleSummaryDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.RoleAssignment
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentScheduleVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyRoleAssignedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyScheduleCanceledEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyScheduleCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StudyScheduleServiceImpl(
    private val groupMemberRepository: StudyGroupMemberRepository,
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val saveStudyGroupPort: SaveStudyGroupPort,
    private val studyScheduleRepository: StudyScheduleRepository,
    private val participantRepository: ScheduleParticipantRepository,
    private val eventPublisher: ApplicationEventPublisher
) : StudyScheduleService {

    override fun createSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        command: StudyScheduleCreateCommand
    ): StudyScheduleDto {
        val group = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 스터디 그룹입니다.")

        val groupMember = groupMemberRepository.findByGroupIdAndMemberId(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        if ( groupMember.role != GroupMemberRole.OWNER ) {
            throw ForbiddenException(message = "스터디 그룹의 소유자만 스케줄을 생성할 수 있습니다.")
        }

        val lastSessionNumber = studyScheduleRepository.countByGroupId(ObjectId(groupId)).toInt()
        val roles = createScheduleRoles(groupId=ObjectId(groupId), roles = command.roles)

        val newStudySchedule = StudySchedule(
            groupId = ObjectId(groupId),
            title = command.title,
            description = command.description,
            scheduleAt = command.scheduleAt,
            session = lastSessionNumber + 1L,
            location = ScheduleLocation(
                type = command.location.type,
                value = command.location.value
            ),
            roles = roles
        )
        group.updateRecentSchedule(RecentScheduleVo(
            scheduleId = newStudySchedule.id!!,
            title = newStudySchedule.title,
            session = newStudySchedule.session,
            scheduleAt = newStudySchedule.scheduleAt
        ))

        val savedSchedule = studyScheduleRepository.save(newStudySchedule)
        saveStudyGroupPort.persist(group)
        eventPublisher.publishEvent(StudyScheduleCreatedEvent(savedSchedule));
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

        if ( schedule.groupId != ObjectId(groupId) ) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        val group = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 스터디 그룹입니다.")

        if ( group.ownerId.toHexString() != memberInfo.memberId ) {
            throw ForbiddenException(message = "스터디 그룹의 소유자만 스케줄을 수정할 수 있습니다.")
        }

        val groupMember = groupMemberRepository.findByGroupIdAndMemberId(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        schedule.changeScheduleAt(requester = groupMember, newScheduleAt = command.scheduleAt)
        schedule.changeTitle(requester=groupMember,newTitle = command.title)
        schedule.changeDescription(groupMember, newDescription = command.description)
        schedule.changeLocation(
            requester = groupMember,
            newLocation = ScheduleLocation(
                type = command.location.type,
                value = command.location.value
            )
        )
        val roles = createScheduleRoles(groupId = ObjectId(groupId), roles=command.roles)
        schedule.updateRoles(requester = groupMember, newRoles = roles)
        schedule = studyScheduleRepository.save(schedule)
        group.updateRecentSchedule(RecentScheduleVo(
            scheduleId = schedule.id!!,
            title = schedule.title,
            session = schedule.session,
            scheduleAt = schedule.scheduleAt
        ))
        saveStudyGroupPort.persist(group)

        return StudyScheduleDto.from(schedule)
    }

    override fun removeSchedule(memberInfo: MemberInfo, groupId: String, scheduleId: String) {
        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw IllegalArgumentException("존재하지 않는 스케줄입니다.")

        if ( schedule.groupId != ObjectId(groupId) ) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        val group = loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 스터디 그룹입니다.")

        if ( group.ownerId.toHexString() != memberInfo.memberId ) {
            throw ForbiddenException(message = "스터디 그룹의 소유자만 스케줄을 삭제할 수 있습니다.")
        }

        // 스케줄이 미래의 시점이라면 레코드 자체를 삭제한다.
        if ( schedule.scheduleAt.isAfter(LocalDateTime.now()) ) {
            if (group.recentSchedule?.scheduleId?.toHexString() == scheduleId) {
                updateGroupRecentSchedule(group);
            }
            eventPublisher.publishEvent(StudyScheduleCanceledEvent(group, schedule));
            studyScheduleRepository.delete(schedule)
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

        if ( schedule.groupId != ObjectId(groupId) ) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        val groupMember = groupMemberRepository.findByGroupIdAndMemberId(
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
        val groupMember = groupMemberRepository.findByGroupIdAndMemberId(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        if ( groupMember.role != GroupMemberRole.OWNER && groupMember.role != GroupMemberRole.MEMBER ) {
            throw ForbiddenException(message = "스터디 그룹의 멤버만 스케줄을 조회할 수 있습니다.")
        }

        val schedules = studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(ObjectId(groupId))
        return schedules.map { schedule ->
            StudyScheduleSummaryDto.from(schedule)
        }
    }

    override fun joinSchedule(memberInfo: MemberInfo, groupId: String, scheduleId: String): StudyScheduleParticipantDto {
        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw IllegalArgumentException("존재하지 않는 스케줄입니다.")

        if ( schedule.groupId != ObjectId(groupId) ) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        if ( participantRepository.existsByScheduleIdAndMemberId(
                scheduleId = ObjectId(scheduleId),
                memberId = ObjectId(memberInfo.memberId)
        )) {
            throw ConflictException(message="이미 해당 스케줄에 참여하고 있습니다.")
        }

        val groupMember = groupMemberRepository.findByGroupIdAndMemberId(
            groupId = ObjectId(groupId),
            memberId = ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message = "가입된 그룹이 아닙니다.")

        val scheduleMember = ScheduleParticipant(
            memberId = ObjectId(memberInfo.memberId),
            scheduleId = ObjectId(scheduleId),
        )

        val participant = participantRepository.save(scheduleMember)
        schedule.increaseParticipantCount()
        studyScheduleRepository.save(schedule)

        return StudyScheduleParticipantDto.of(
            schedule = schedule,
            member = groupMember
        )
    }

    override fun leaveSchedule(memberInfo: MemberInfo, groupId: String, scheduleId: String): Unit {
        val schedule = studyScheduleRepository.loadById(ObjectId(scheduleId))
            ?: throw IllegalArgumentException("존재하지 않는 스케줄입니다.")

        if ( schedule.groupId != ObjectId(groupId) ) {
            throw ForbiddenException(message = "해당 그룹의 스케줄이 아닙니다.")
        }

        if ( schedule.scheduleAt.isBefore(LocalDateTime.now()) ) {
            throw BadRequestException(message = "과거의 스케줄은 참여를 취소할 수 없습니다.")
        }

        val participant = participantRepository.getByScheduleIdAndMemberId(schedule.id!!, ObjectId(memberInfo.memberId))
            ?: throw IllegalArgumentException("해당 스케줄에 참여하지 않은 멤버입니다.")

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

        val members = groupMemberRepository.findByGroupIdAndMemberIdsIn(
            groupId = schedule.groupId,
            memberIds = memberIds
        )

        return members.map {member->
            StudyScheduleParticipantDto.of(schedule, member)
        }
    }

    private fun createScheduleRoles(groupId: ObjectId, roles: List<RoleAssignment>)
    : MutableSet<ScheduleRole> {

        val memberIds = roles.map { ObjectId(it.memberId) }
            .toSet()
            .toList()

        val members = groupMemberRepository.findByGroupIdAndMemberIdsIn(groupId, memberIds)
            .associate{ it.memberId.toHexString() to it }
            .toMap()

        return roles.map { role ->
            val member = members[role.memberId]
                ?: throw BadRequestException(message="존재하지 않는 멤버입니다.")
            ScheduleRole(
                memberId = member.memberId,
                nickname = member.nickname,
                image = ProfileImageVo(member.profileImage.type, member.profileImage.url),
                color = role.color,
                roles = role.roles
            )
        }.toMutableSet()
    }

    private fun updateGroupRecentSchedule(group: StudyGroup) {
        val schedules = studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(group.id!!)
        if (schedules.isEmpty()) {
            group.updateRecentSchedule(null)
            saveStudyGroupPort.persist(group)
            return
        }

        val now = LocalDateTime.now()
        val recentSchedule = schedules.firstOrNull { it.scheduleAt.isAfter(now) }
        if ( recentSchedule == null ) {
            group.updateRecentSchedule(null)
        } else {
            group.updateRecentSchedule(RecentScheduleVo(
                scheduleId = recentSchedule.id!!,
                title = recentSchedule.title,
                session = recentSchedule.session,
                scheduleAt = recentSchedule.scheduleAt
            ))
        }
        saveStudyGroupPort.persist(group)
    }
}