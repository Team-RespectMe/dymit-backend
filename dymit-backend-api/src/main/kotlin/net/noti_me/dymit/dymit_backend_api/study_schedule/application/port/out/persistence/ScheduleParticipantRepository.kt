package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import org.bson.types.ObjectId

interface ScheduleParticipantRepository {

    fun save(participant: ScheduleParticipant): ScheduleParticipant

    fun delete(participant: ScheduleParticipant): Boolean

    fun getByScheduleIdAndMemberId(scheduleId: ObjectId, memberId: ObjectId): ScheduleParticipant?
    fun getByScheduleId(scheduleId: ObjectId): List<ScheduleParticipant>

    fun existsByScheduleIdAndMemberId(scheduleId: ObjectId, memberId: ObjectId): Boolean
}
