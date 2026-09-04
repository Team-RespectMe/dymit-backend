package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.types.ObjectId
import java.time.Instant

interface StudyScheduleRepository {

    fun loadByGroupIdOrderByScheduleAtDesc(studyGroupId: ObjectId): List<StudySchedule>

    fun loadById(id: ObjectId): StudySchedule?

    fun save(schedule: StudySchedule): StudySchedule

    fun delete(schedule: StudySchedule): Boolean

    fun deleteById(id: ObjectId): Boolean

    fun countByGroupId(studyGroupId: ObjectId): Long

    fun findFirstAfterByGroupIdsOrderByScheduleAtAsc(groupIds: List<ObjectId>, now: Instant): Map<ObjectId, StudySchedule?>

    fun findByScheduleAtBetweenCursorPagination(
        start: Instant,
        end: Instant,
        cursor: ObjectId?,
        limit: Int = 1000
    ): List<StudySchedule>
}
