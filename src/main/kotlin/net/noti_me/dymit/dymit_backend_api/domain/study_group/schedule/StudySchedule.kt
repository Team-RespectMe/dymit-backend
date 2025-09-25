package net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.ScheduleTimeChangedEvent
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
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

//    @Id
//    var id: ObjectId = id
//        private set

//    val identifier: String
//        get() = id.toHexString()
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
        registerEvent(ScheduleTimeChangedEvent(this))
    }

    fun changeLocation(
        requester: StudyGroupMember,
        newLocation: ScheduleLocation
    ) {
        checkDefaultPermissions(requester)
        if ( newLocation.type == this.location.type && newLocation.value == this.location.value ) {
            return // No change
        }
        this.location = newLocation
        registerEvent(ScheduleTimeChangedEvent(this))
    }

    fun updateRoles(
        requester: StudyGroupMember,
        newRoles: Set<ScheduleRole>
    ) {
        checkDefaultPermissions(requester)

        val oldRolesByMember = this.roles.associateBy { it.memberId }
        val newRolesByMember = newRoles.associateBy { it.memberId }
        val updatedRoles = mutableSetOf<ScheduleRole>()

        // 기존 멤버 중 역할이 변경된 경우 교체
        for (oldRole in this.roles) {
            val newRole = newRoles.find { it.memberId == oldRole.memberId }
            if (newRole != null) {
                if (oldRole.roles != newRole.roles) {
                    updatedRoles.add(newRole)
                } else {
                    updatedRoles.add(oldRole)
                }
            }
        }

        // 삭제된 멤버 역할 제거
        val removedMemberIds = this.roles.map { it.memberId }.toSet() - newRoles.map { it.memberId }.toSet()
        val addedRoles = newRoles.filter { newRole -> this.roles.none { it.memberId == newRole.memberId } }
        if (addedRoles.isNotEmpty()) {
            updatedRoles.addAll(addedRoles)
        }
        // 삭제된 멤버는 updatedRoles에 추가하지 않음
        updatedRoles.removeAll { it.memberId in removedMemberIds }
        this.roles.clear()
        this.roles.addAll(updatedRoles)
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

    override fun equals(other: Any?): Boolean {
        if ( this === other ) return true
        if ( other !is StudySchedule ) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}