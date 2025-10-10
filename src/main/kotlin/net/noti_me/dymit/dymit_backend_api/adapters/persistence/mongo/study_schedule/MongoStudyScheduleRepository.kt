package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_schedule

import com.mongodb.client.model.Aggregates.group
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.ROOT
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MongoStudyScheduleRepository(
    private val mongoTemplate: MongoTemplate
): StudyScheduleRepository {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun save(schedule: StudySchedule): StudySchedule {
        return mongoTemplate.save(schedule)
    }

    override fun delete(schedule: StudySchedule): Boolean {
        return mongoTemplate.remove(schedule).deletedCount > 0
    }

    override fun deleteById(id: ObjectId): Boolean {
        val query = Query(Criteria.where("_id").`is`(id))
        val result = mongoTemplate.remove(query, StudySchedule::class.java)
        return result.deletedCount > 0
    }

    override fun loadByGroupIdOrderByScheduleAtDesc(studyGroupId: ObjectId): List<StudySchedule> {
        val query = Query(Criteria.where("groupId").`is`(studyGroupId))
            .with(Sort.by(Sort.Direction.DESC, "scheduleAt"))
        return mongoTemplate.find(query, StudySchedule::class.java)
    }

    override fun loadById(id: ObjectId): StudySchedule? {
        return mongoTemplate.findById(id, StudySchedule::class.java)
    }

    override fun countByGroupId(studyGroupId: ObjectId): Long {
        val query = Query(Criteria.where("groupId").`is`(studyGroupId))
        return mongoTemplate.count(query, StudySchedule::class.java)
    }

    override fun findFirstAfterByGroupIdsOrderByScheduleAtAsc(
        groupIds: List<ObjectId>,
        now: LocalDateTime
    ): Map<ObjectId, StudySchedule?> {
        val matchOperation = Aggregation.match(
            Criteria.where("groupId").`in`(groupIds)
                .and("scheduleAt").gt(now)
        )
        val sortOperation = Aggregation.sort(Sort.Direction.ASC, "scheduleAt")
        val groupOperation = Aggregation.group("groupId")
            .first(Aggregation.ROOT).`as`("schedule")
        val aggregation = Aggregation.newAggregation(
            matchOperation,
            sortOperation,
            groupOperation
        )

        val results = mongoTemplate.aggregate(
            aggregation,
            "study_schedules", // Collection name for Schedule documents
            Document::class.java
        )

        logger.debug("Found {} results", results.mappedResults.size)

        // 결과 디버깅을 위해 각 문서 내용 확인
        if (results.mappedResults.isNotEmpty()) {
            val firstResult = results.mappedResults.first()
            logger.debug("First result document keys: {}", firstResult.keys)
            logger.debug("First result document content: {}", firstResult)
        }

        // 결과를 임시 맵에 저장
        val scheduleMap = mutableMapOf<ObjectId, StudySchedule?>()

        // 모든 결과 처리 시도 (예외 발생 시 로깅)
        results.mappedResults.forEach { document ->
            try {
                val groupId = document["_id"] as ObjectId

                // schedule 필드가 없거나 null인 경우 처리
                val scheduleDoc = document["schedule"]
                if (scheduleDoc != null && scheduleDoc is Document) {
                    val schedule = mongoTemplate.converter.read(
                        StudySchedule::class.java,
                        scheduleDoc
                    )
                    scheduleMap[groupId] = schedule
                    logger.debug("Mapped schedule for group ID: {}", groupId)
                } else {
                    logger.warn("Missing or invalid schedule field for group ID: {}", groupId)
                    scheduleMap[groupId] = null
                }
            } catch (e: Exception) {
                logger.error("Error processing document: {}", e.message, e)
                logger.debug("Problem document: {}", document)
            }
        }

        // 모든 요청된 그룹에 대해 결과 맵 생성 (없는 그룹은 null로 매핑)
        return groupIds.associateWith { scheduleMap[it] }
    }
}