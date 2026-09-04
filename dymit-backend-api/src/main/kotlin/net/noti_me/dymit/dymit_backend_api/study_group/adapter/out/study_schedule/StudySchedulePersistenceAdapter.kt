package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.study_schedule

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.study_schedule.StudyGroupSchedulePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.study_schedule.dto.StudyGroupScheduleData
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

@Component
class StudySchedulePersistenceAdapter(
    private val mongoTemplate: MongoTemplate
) : StudyGroupSchedulePort {

    override fun loadUpcomingByGroupIds(
        groupIds: List<String>
    ): Map<String, StudyGroupScheduleData> {
        if (groupIds.isEmpty()) {
            return emptyMap()
        }

        val groupObjectIds = groupIds.map(::ObjectId)
        val now = Date.from(Instant.now())
        val query = Query(
            Criteria.where("groupId").`in`(groupObjectIds)
                .and("scheduleAt").gt(now)
        ).with(Sort.by(Sort.Direction.ASC, "scheduleAt"))

        return buildMap {
            mongoTemplate.find(query, Document::class.java, SCHEDULE_COLLECTION)
                .mapNotNull(::toScheduleData)
                .forEach { schedule -> putIfAbsent(schedule.groupId, schedule) }
        }
    }

    private fun toScheduleData(document: Document): StudyGroupScheduleData? {
        val id = document.getObjectId("_id") ?: return null
        val groupId = document.getObjectId("groupId") ?: return null
        val scheduleAt = document["scheduleAt"].toInstant() ?: return null
        return StudyGroupScheduleData(
            id = id.toHexString(),
            groupId = groupId.toHexString(),
            title = document.getString("title"),
            session = (document["session"] as? Number)?.toLong() ?: 1L,
            scheduleAt = scheduleAt
        )
    }

    private fun Any?.toInstant(): Instant? =
        when (this) {
            is Instant -> this
            is Date -> toInstant()
            else -> null
        }

    companion object {
        private const val SCHEDULE_COLLECTION = "study_schedules"
    }
}
