package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleAttachment
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleAttachmentLinkQueryRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleAttachmentRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * 스터디 일정 첨부 관계를 MongoDB에 저장하는 Repository 구현체입니다.
 *
 * @param mongoTemplate MongoTemplate 인스턴스
 */
@Repository
class MongoScheduleAttachmentRepository(
    private val mongoTemplate: MongoTemplate
) : ScheduleAttachmentRepository, ScheduleAttachmentLinkQueryRepository {

    override fun findByScheduleId(scheduleId: ObjectId): List<ScheduleAttachment> {
        val query = Query(Criteria.where("scheduleId").`is`(scheduleId))
            .with(Sort.by(Sort.Direction.ASC, "createdAt"))
        return mongoTemplate.find(query, ScheduleAttachment::class.java)
    }

    override fun findAttachedFileIdsExcludingSchedule(
        fileIds: List<ObjectId>,
        scheduleId: ObjectId
    ): Set<ObjectId> {
        if ( fileIds.isEmpty() ) {
            return emptySet()
        }

        val query = Query(
            Criteria.where("fileId").`in`(fileIds)
                .and("scheduleId").ne(scheduleId)
        )
        return mongoTemplate.find(query, ScheduleAttachment::class.java)
            .map { it.fileId }
            .toSet()
    }

    override fun replaceByScheduleId(
        scheduleId: ObjectId,
        attachments: List<ScheduleAttachment>
    ): List<ScheduleAttachment> {
        val deleteQuery = Query(Criteria.where("scheduleId").`is`(scheduleId))
        mongoTemplate.remove(deleteQuery, ScheduleAttachment::class.java)

        if ( attachments.isEmpty() ) {
            return emptyList()
        }

        return mongoTemplate.insertAll(attachments).map { it as ScheduleAttachment }
    }
}
