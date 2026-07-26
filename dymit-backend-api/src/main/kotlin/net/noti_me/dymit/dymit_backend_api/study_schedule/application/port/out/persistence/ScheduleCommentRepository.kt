package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.dto.ScheduleCommentWriterUpdateDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleComment
import org.bson.types.ObjectId
import java.util.*

interface ScheduleCommentRepository {

    fun save(scheduleComment: ScheduleComment): ScheduleComment

    fun findById(id: ObjectId): ScheduleComment?

    fun findByMemberId(memberId: ObjectId): List<ScheduleComment>

    fun findByScheduleId(scheduleId: ObjectId, cursor: ObjectId?, size: Long): List<ScheduleComment>

    fun deleteById(id: ObjectId)

    fun updateWriterInfo(writer: ScheduleCommentWriterUpdateDto): Int
}
