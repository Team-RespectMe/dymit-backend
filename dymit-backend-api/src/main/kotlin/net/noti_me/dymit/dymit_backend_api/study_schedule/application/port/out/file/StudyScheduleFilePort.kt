package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto.StudyScheduleFileDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto.StudyScheduleFileStatusDto
import org.bson.types.ObjectId

interface StudyScheduleFilePort {

    fun loadByIds(fileIds: List<ObjectId>): List<StudyScheduleFileDto>

    fun updateStatus(fileId: ObjectId, status: StudyScheduleFileStatusDto): StudyScheduleFileStatusDto?
}
