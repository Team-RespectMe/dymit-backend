package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleParticipant
import org.bson.types.ObjectId

interface ScheduleParticipantRepository {

    fun save(participant: ScheduleParticipant): ScheduleParticipant

    fun delete(participant: ScheduleParticipant): Boolean

    fun getByScheduleIdAndMemberId(scheduleId: ObjectId, memberId: ObjectId): ScheduleParticipant?

    fun getByScheduleId(scheduleId: ObjectId): List<ScheduleParticipant>

    fun existsByScheduleIdAndMemberId(scheduleId: ObjectId, memberId: ObjectId): Boolean
}