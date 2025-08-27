package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import org.bson.types.ObjectId

interface StudyScheduleRepository {

    fun loadByGroupIdOrderByScheduleAtDesc(studyGroupId: ObjectId): List<StudySchedule>

    fun loadById(id: ObjectId): StudySchedule?

    fun save(schedule: StudySchedule): StudySchedule

    fun delete(schedule: StudySchedule): Boolean

    fun deleteById(id: ObjectId): Boolean

    fun countByGroupId(studyGroupId: ObjectId): Long
}