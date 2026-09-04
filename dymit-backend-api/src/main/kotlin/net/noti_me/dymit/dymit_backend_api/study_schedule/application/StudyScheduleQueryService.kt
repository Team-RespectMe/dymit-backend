package net.noti_me.dymit.dymit_backend_api.study_schedule.application

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleQueryPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.StudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StudyScheduleQueryService(
    private val scheduleRepository: StudyScheduleRepository,
    private val participantRepository: ScheduleParticipantRepository
) : StudyScheduleQueryPort {

    override fun loadById(scheduleId: ObjectId): StudyScheduleServerDto? {
        return scheduleRepository.loadById(scheduleId)?.toServerDto()
    }

    override fun loadByGroupIdOrderByScheduleAtDesc(groupId: ObjectId): List<StudyScheduleServerDto> {
        return scheduleRepository.loadByGroupIdOrderByScheduleAtDesc(groupId).map { it.toServerDto() }
    }

    override fun findByScheduleAtBetween(
        start: Instant,
        end: Instant,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyScheduleServerDto> {
        return scheduleRepository.findByScheduleAtBetweenCursorPagination(
            start = start,
            end = end,
            cursor = cursor,
            limit = limit
        ).map { it.toServerDto() }
    }

    override fun getParticipantMemberIds(scheduleId: ObjectId): List<ObjectId> {
        return participantRepository.getByScheduleId(scheduleId).map { it.memberId }
    }

    override fun existsParticipant(scheduleId: ObjectId, memberId: ObjectId): Boolean {
        return participantRepository.existsByScheduleIdAndMemberId(scheduleId, memberId)
    }

    private fun StudySchedule.toServerDto() = StudyScheduleServerDto(
        id = id!!,
        groupId = groupId,
        title = title,
        session = session,
        scheduleAt = scheduleAt
    )
}
