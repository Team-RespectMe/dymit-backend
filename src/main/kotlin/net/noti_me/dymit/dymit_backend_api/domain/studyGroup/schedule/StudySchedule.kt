package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection="study_schedules")
class StudySchedule(
    id: ObjectId = ObjectId.get(),
    groupId: ObjectId = ObjectId.get(),
    title: String = "",
    session: Int = 1,
    scheduleAt: LocalDateTime,
    assignments: MutableList<ScheduleAssignment> = mutableListOf(),
    roles: MutableList<ScheduleRole> = mutableListOf()
) : BaseAggregateRoot<StudySchedule>() {

    @Id
    var id: ObjectId = id
        private set

    val identifier: String
        get() = id.toHexString()

    var groupId: ObjectId = groupId
        private set

    var title: String = title
        private set

    var scheduleAt: LocalDateTime = scheduleAt
        private set

    var assignments: MutableList<ScheduleAssignment> = assignments
        private set

    var roles: MutableList<ScheduleRole> = roles
        private set

    fun addAssignment(assignment: ScheduleAssignment) {
        assignments.add(assignment)
    }

    fun deleteAssignment(assignment: ScheduleAssignment) {
        if (!assignments.remove(assignment)) {
            throw IllegalArgumentException("Assignment not found in the schedule")
        }
    }

    fun clearAssignments() {
        assignments.clear()
    }

    fun addRole(role: ScheduleRole) {
        roles.add(role)
    }

    fun deleteRole(role: ScheduleRole) {
        if (!roles.remove(role)) {
            throw IllegalArgumentException("Role not found in the schedule")
        }
    }

    fun clearRoles() {
        roles.clear()
    }

    fun changeTitle(newTitle: String) {
        this.title = newTitle
    }

    fun changeScheduleAt(newScheduleAt: LocalDateTime) {
        this.scheduleAt = newScheduleAt
    }
}