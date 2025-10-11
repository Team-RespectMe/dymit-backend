package net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.ScheduleLocationChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.ScheduleTimeChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyRoleChangedEvent
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection="study_schedules")
class StudySchedule(
//    id: ObjectId = ObjectId.get(),
    id: ObjectId? = null,
    groupId: ObjectId = ObjectId.get(),
    title: String = "",
    description: String = "",
    location: ScheduleLocation = ScheduleLocation(),
    val session: Long = 1,
    scheduleAt: LocalDateTime,
    roles: MutableSet<ScheduleRole> = mutableSetOf(),
    nrParticipant: Long = 0L,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
) : BaseAggregateRoot<StudySchedule>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    @Indexed(name = "study_schedule_group_id_idx")
    var groupId: ObjectId = groupId
        private set

    var title: String = title
        private set

    var description: String = description
        private set

    var scheduleAt: LocalDateTime = scheduleAt
        private set

    var location: ScheduleLocation = location
        private set

    var roles = roles
        private set

    var nrParticipant: Long = nrParticipant
        private set

    fun isExpired(): Boolean {
        return scheduleAt.isBefore(LocalDateTime.now())
    }

    fun changeTitle(
        requester: StudyGroupMember,
        newTitle: String
    ) {
        checkDefaultPermissions(requester)

        if ( newTitle.length > 30 ) {
            throw IllegalArgumentException("제목은 30자 이내로 작성해야 합니다.")
        }

        this.title = newTitle
    }

    fun changeDescription(
        requester: StudyGroupMember,
        newDescription: String
    ) {
        checkDefaultPermissions(requester)

        if ( newDescription.length > 100 ) {
            throw IllegalArgumentException("설명은 100자 이내로 작성해야 합니다.")
        }
        this.description = newDescription
    }

    fun changeScheduleAt(
        requester: StudyGroupMember,
        group: StudyGroup,
        newScheduleAt: LocalDateTime
    ) {
        checkDefaultPermissions(requester)

        if ( newScheduleAt.isBefore(LocalDateTime.now()) ) {
            throw IllegalArgumentException("새로운 시간은 현재 시간 이후여야 합니다.")
        }

        if ( scheduleAt.isBefore(LocalDateTime.now()) ) {
            throw IllegalArgumentException("이미 지나간 일정의 예정 시간은 변경할 수 없습니다.")
        }

        this.scheduleAt = newScheduleAt
        registerEvent(ScheduleTimeChangedEvent(group = group, schedule = this))
    }

    fun changeLocation(
        requester: StudyGroupMember,
        group: StudyGroup,
        newLocation: ScheduleLocation
    ) {
        checkDefaultPermissions(requester)
        if ( newLocation.type == this.location.type && newLocation.value == this.location.value ) {
            return // No change
        }
        this.location = newLocation
        registerEvent(ScheduleLocationChangedEvent(group = group, schedule = this))
    }

    private fun addNewRole(newRole: ScheduleRole) {
        roles.add(newRole)
    }

    private fun updateExistingRole(group: StudyGroup, target: ScheduleRole, newRole: ScheduleRole) {
        if ( target.isRoleChanged(newRole) ) {
            registerEvent(StudyRoleChangedEvent(group = group, schedule= this, role = newRole))
        }
        roles.remove(target)
        roles.add(newRole)
    }

    private fun addRole(
        group: StudyGroup,
        newRole: ScheduleRole
    ) {
        roles.firstOrNull { it.memberId == newRole.memberId }
            ?.let { updateExistingRole(group = group, target = it, newRole = newRole) }
            ?: addNewRole(newRole)
    }

    fun updateRoles(
        requester: StudyGroupMember,
        group: StudyGroup,
        newRoles: Set<ScheduleRole>
    ) {
        checkDefaultPermissions(requester)
        val rolesToRemove = roles.filter { existingRole ->
            newRoles.none { it.memberId == existingRole.memberId }
        }
        rolesToRemove.forEach { this.roles.remove(it) }
        newRoles.forEach { newRole -> addRole(group = group, newRole =newRole) }
    }

    fun increaseParticipantCount() {
        this.nrParticipant++
    }

    fun decreaseParticipantCount() {
        if ( this.nrParticipant > 0 ) {
            this.nrParticipant--
        }
    }

    private fun checkDefaultPermissions(requester: StudyGroupMember) {
        if ( requester.role != GroupMemberRole.OWNER
            && requester.role != GroupMemberRole.ADMIN  ) {
            throw ForbiddenException(message = "권한이 없습니다.")
        }

        if ( requester.groupId != groupId ) {
            throw ForbiddenException(message = "소속된 그룹이 아닙니다.")
        }
    }

    fun isRoleAssigned(memberId: ObjectId): Boolean {
        return roles.any { it.memberId == memberId }
    }

    override fun equals(other: Any?): Boolean {
        if ( this === other ) return true
        if ( other !is StudySchedule ) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}