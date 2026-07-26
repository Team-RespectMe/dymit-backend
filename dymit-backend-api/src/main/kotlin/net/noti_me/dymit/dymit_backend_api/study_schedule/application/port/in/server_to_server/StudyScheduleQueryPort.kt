package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto
import org.bson.types.ObjectId
import java.time.LocalDateTime

interface StudyScheduleQueryPort {

    fun loadById(scheduleId: ObjectId): StudyScheduleServerDto?

    fun loadByGroupIdOrderByScheduleAtDesc(groupId: ObjectId): List<StudyScheduleServerDto>

    fun findByScheduleAtBetween(
        start: LocalDateTime,
        end: LocalDateTime,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyScheduleServerDto>

    fun getParticipantMemberIds(scheduleId: ObjectId): List<ObjectId>

    fun existsParticipant(scheduleId: ObjectId, memberId: ObjectId): Boolean
}
