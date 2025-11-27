package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import org.bson.types.ObjectId
import java.util.*

interface ScheduleCommentRepository {

    fun save(scheduleComment: ScheduleComment): ScheduleComment

    fun findById(id: ObjectId): ScheduleComment?

    fun findByMemberId(memberId: ObjectId): List<ScheduleComment>

    fun findByScheduleId(scheduleId: ObjectId, cursor: ObjectId?, size: Long): List<ScheduleComment>

    fun deleteById(id: ObjectId)

    fun updateWriterInfo(member: Member): Int
}
