package net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.statistics

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleStatisticsQueryPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipationStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsBoundaryDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsChanges
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsQuery
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleStatisticsReferenceDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.time.Instant

/** MongoDB에서 일정 통계 원천을 증분 조회합니다. */
@Component
class MongoStudyScheduleStatisticsQueryAdapter(
    private val mongoTemplate: MongoTemplate
) : StudyScheduleStatisticsQueryPort {

    override fun loadSessionBoundaries(
        groupIds: List<ObjectId>,
        observedAt: Instant
    ): List<StudyScheduleStatisticsBoundaryDto> {
        if (groupIds.isEmpty()) {
            return emptyList()
        }
        return mongoTemplate.find(
            Query(
                Criteria.where("groupId").`in`(groupIds)
                    .and("isDeleted").ne(true)
                    .and("scheduleAt").lt(observedAt)
            ).with(
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Order.asc("scheduleAt"),
                    org.springframework.data.domain.Sort.Order.asc("_id")
                )
            ),
            StudySchedule::class.java
        ).map { schedule ->
            StudyScheduleStatisticsBoundaryDto(
                groupId = schedule.groupId,
                scheduleId = requireNotNull(schedule.id),
                session = schedule.session,
                scheduleAt = schedule.scheduleAt
            )
        }
    }

    override fun loadGroupIdsHavingSchedule(groupIds: List<ObjectId>): Set<ObjectId> {
        if (groupIds.isEmpty()) {
            return emptySet()
        }
        return mongoTemplate.findDistinct(
            Query(
                Criteria.where("groupId").`in`(groupIds)
                    .and("isDeleted").ne(true)
            ),
            "groupId",
            StudySchedule::class.java,
            ObjectId::class.java
        ).toSet()
    }

    override fun loadChanged(query: StudyScheduleStatisticsQuery): StudyScheduleStatisticsChanges {
        if (query.previousCutoff == null || query.mutationAfter == null) {
            return StudyScheduleStatisticsChanges(emptyList(), requiresReplay = true)
        }
        val overlap = query.mutationAfter.minusSeconds(CURSOR_OVERLAP_SECONDS)
        val changeLowerBound = if (query.cutoff > query.previousCutoff) {
            minOf(overlap, query.previousCutoff)
        } else {
            overlap
        }
        val matured = mongoTemplate.find(
            Query(
                Criteria.where("groupId").`is`(query.groupId)
                    .and("createdAt").gte(query.joinedAt)
                    .and("scheduleAt").gt(query.previousCutoff).lte(query.cutoff)
            ),
            StudySchedule::class.java
        )
        val changedSchedules = mongoTemplate.find(
            Query(
                Criteria.where("groupId").`is`(query.groupId)
                    .and("updatedAt").gte(changeLowerBound).lte(query.mutationThrough)
            ),
            StudySchedule::class.java
        )
        val changedParticipants = mongoTemplate.find(
            Query(
                Criteria.where("memberId").`is`(query.memberId)
                    .and("updatedAt").gte(changeLowerBound).lte(query.mutationThrough)
            ),
            ScheduleParticipant::class.java
        )
        val participantScheduleIds = changedParticipants.map { it.scheduleId }.distinct()
        val participantSchedules = if (participantScheduleIds.isEmpty()) {
            emptyList()
        } else {
            mongoTemplate.find(
                Query(
                    Criteria.where("_id").`in`(participantScheduleIds)
                        .and("groupId").`is`(query.groupId)
                        .and("createdAt").gte(query.joinedAt)
                ),
                StudySchedule::class.java
            )
        }
        val maturedIds = matured.mapNotNull { it.id }.toSet()
        val correctionIds = (changedSchedules + participantSchedules)
            .mapNotNull { it.id }
            .filterNot(maturedIds::contains)
        val schedules = (matured + changedSchedules + participantSchedules).distinctBy { it.id }
        return StudyScheduleStatisticsChanges(
            sources = mapSources(schedules, query.memberId),
            requiresReplay = correctionIds.isNotEmpty()
        )
    }

    override fun loadAll(query: StudyScheduleStatisticsQuery): List<StudyScheduleStatisticsDto> {
        val schedules = mongoTemplate.find(
            Query(
                Criteria.where("groupId").`is`(query.groupId)
                    .and("createdAt").gte(query.joinedAt)
            ),
            StudySchedule::class.java
        )
        return mapSources(schedules, query.memberId)
    }

    override fun loadScheduleReferences(groupId: ObjectId): List<StudyScheduleStatisticsReferenceDto> {
        return mongoTemplate.find(
            Query(Criteria.where("groupId").`is`(groupId)).apply {
                fields().include("_id").include("deletedAt").include("updatedAt")
            },
            StudySchedule::class.java
        ).mapNotNull { schedule ->
            schedule.id?.let {
                StudyScheduleStatisticsReferenceDto(
                    scheduleId = it,
                    deletedAt = schedule.deletedAt,
                    updatedAt = schedule.updatedAt
                )
            }
        }
    }

    private fun mapSources(
        schedules: List<StudySchedule>,
        memberId: ObjectId
    ): List<StudyScheduleStatisticsDto> {
        val ids = schedules.mapNotNull { it.id }
        val participants = if (ids.isEmpty()) {
            emptyList()
        } else {
            mongoTemplate.find(
                Query(
                    Criteria.where("scheduleId").`in`(ids)
                        .and("memberId").`is`(memberId)
                ),
                ScheduleParticipant::class.java
            )
        }.groupBy { it.scheduleId }
        return schedules.mapNotNull { schedule ->
            val createdAt = schedule.createdAt ?: return@mapNotNull null
            StudyScheduleStatisticsDto(
                scheduleId = requireNotNull(schedule.id),
                session = schedule.session,
                createdAt = createdAt,
                scheduleAt = schedule.scheduleAt,
                deletedAt = schedule.deletedAt,
                participations = participants[schedule.id].orEmpty().mapNotNull { participant ->
                    participant.createdAt?.let {
                        StudyScheduleParticipationStatisticsDto(
                            participatedAt = it,
                            deletedAt = participant.deletedAt
                        )
                    }
                }
            )
        }
    }

    private companion object {
        const val CURSOR_OVERLAP_SECONDS = 1L
    }
}
