package net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.ScheduleTimeChangedEvent
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection="study_schedules")
class StudySchedule(
    id: ObjectId = ObjectId.get(),
    groupId: ObjectId = ObjectId.get(),
    title: String = "",
    description: String = "",
    location: ScheduleLocation = ScheduleLocation(),
    val session: Long = 1,
    scheduleAt: LocalDateTime,
    roles: MutableSet<ScheduleRole> = mutableSetOf(),
    nrParticipant: Long = 0L
) : BaseAggregateRoot<StudySchedule>() {

    @Id
    var id: ObjectId = id
        private set

    val identifier: String
        get() = id.toHexString()

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

    fun changeTitle(newTitle: String) {
        if ( title.length > 30 ) {
            throw IllegalArgumentException("Title cannot exceed 30 characters")
        }
        this.title = newTitle
    }

    fun changeDescription(newDescription: String) {
        if ( description.length > 100 ) {
            throw IllegalArgumentException("Description cannot exceed 100 characters")
        }
        this.description = newDescription
    }

    fun changeScheduleAt(newScheduleAt: LocalDateTime) {
        if ( newScheduleAt.isBefore(LocalDateTime.now()) ) {
            throw IllegalArgumentException("Schedule time cannot be in the past")
        }
        this.scheduleAt = newScheduleAt
        registerEvent(ScheduleTimeChangedEvent(this))
    }

    fun changeLocation(newLocation: ScheduleLocation) {
        if ( newLocation.type == this.location.type && newLocation.value == this.location.value ) {
            return // No change
        }
        this.location = newLocation
        registerEvent(ScheduleTimeChangedEvent(this))
    }

    fun updateRoles(newRoles: Set<ScheduleRole>) {
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

    override fun equals(other: Any?): Boolean {
        if ( this === other ) return true
        if ( other !is StudySchedule ) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}