package net.noti_me.dymit.dymit_backend_api.study_schedule.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Represents a member's participation in a study schedule.
 */
@Document(collection = "study_schedule_participants")
@TypeAlias("net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleParticipant")
@CompoundIndex(
    name = "schedule_participant_created_at_member_id_idx",
    def = "{'createdAt': 1, 'memberId': 1}"
)
class ScheduleParticipant(
    @Id
    val id: ObjectId = ObjectId.get(),
    @Indexed(name = "schedule_participant_schedule_id_idx")
    val scheduleId: ObjectId = ObjectId.get(),
    @Indexed(name = "schedule_participant_member_id_idx")
    val memberId: ObjectId = ObjectId.get(),
    val createdAt: LocalDateTime? = null,
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
