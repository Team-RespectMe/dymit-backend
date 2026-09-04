package net.noti_me.dymit.dymit_backend_api.reminder.adapter.`out`.study_schedule

import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.ReminderStudySchedulePort
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.dto.ReminderStudyScheduleDto
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Reminder 일정 조회 포트를 기존 MongoDB 컬렉션에 연결합니다.
 */
@Component
class MongoReminderStudyScheduleAdapter(
    private val mongoTemplate: MongoTemplate
) : ReminderStudySchedulePort {

    /**
     * 일정 문서를 조회해 Reminder 소유 DTO로 변환합니다.
     */
    override fun findByScheduleAtBetween(
        start: Instant,
        end: Instant,
        cursor: ObjectId?,
        limit: Int
    ): List<ReminderStudyScheduleDto> {
        val criteria = Criteria.where("scheduleAt").gte(start).lt(end)
        if (cursor != null) {
            criteria.and("_id").gt(cursor)
        }
        val query = Query(criteria)
            .with(Sort.by(Sort.Direction.DESC, "_id"))
            .limit(limit)
        return mongoTemplate.find(
            query,
            ReminderStudyScheduleDocument::class.java,
            SCHEDULE_COLLECTION_NAME
        ).map {
            ReminderStudyScheduleDto(
                id = requireNotNull(it.id),
                groupId = it.groupId,
                title = it.title,
                session = it.session,
                scheduleAt = it.scheduleAt
            )
        }
    }

    /**
     * 일정 참여 문서에서 회원 식별자만 조회합니다.
     */
    override fun getParticipantMemberIds(scheduleId: ObjectId): List<ObjectId> {
        val query = Query(Criteria.where("scheduleId").`is`(scheduleId))
        query.fields().include("memberId")
        return mongoTemplate.find(
            query,
            Document::class.java,
            PARTICIPANT_COLLECTION_NAME
        ).mapNotNull { it.getObjectId("memberId") }
    }

    /**
     * 기존 일정 MongoDB 문서의 어댑터 내부 투영입니다.
     */
    private data class ReminderStudyScheduleDocument(
        val id: ObjectId? = null,
        val groupId: ObjectId = ObjectId.get(),
        val title: String = "",
        val session: Long = 1L,
        val scheduleAt: Instant = Instant.now()
    )

    private companion object {
        const val SCHEDULE_COLLECTION_NAME = "study_schedules"
        const val PARTICIPANT_COLLECTION_NAME = "study_schedule_participants"
    }
}
