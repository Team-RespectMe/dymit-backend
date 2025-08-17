package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "study_schedule_participants")
class ScheduleParticipant(
    @Id
    val id: ObjectId = ObjectId.get(),
    @Indexed(name = "schedule_participant_schedule_id_idx")
    val scheduleId: ObjectId = ObjectId.get(),
    @Indexed(name = "schedule_participant_member_id_idx")
    val memberId: ObjectId = ObjectId.get(),
) {
    val identifier: String
        get() = id.toHexString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScheduleParticipant) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}