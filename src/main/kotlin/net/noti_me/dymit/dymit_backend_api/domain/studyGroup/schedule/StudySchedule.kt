package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
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
    location: ScheduleLocation = ScheduleLocation(),
    val session: Int = 1,
    scheduleAt: LocalDateTime,
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

    var scheduleAt: LocalDateTime = scheduleAt
        private set

    var location: ScheduleLocation = location
        private set

    fun changeTitle(newTitle: String) {
        this.title = newTitle
    }

    fun changeScheduleAt(newScheduleAt: LocalDateTime) {
        this.scheduleAt = newScheduleAt
    }

    fun changeLocation(newLocation: ScheduleLocation) {
        this.location = newLocation
    }
}