package net.noti_me.dymit.dymit_backend_api.calendar.adapter.out.study_schedule

import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule.LoadCalendarStudySchedulePort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule.dto.CalendarStudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * 스터디 일정 문서에서 캘린더 이벤트를 조회하는 어댑터입니다.
 *
 * @property mongoTemplate MongoDB 조회 도구
 */
@Repository
class MongoCalendarStudyScheduleAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadCalendarStudySchedulePort {

    /**
     * 회원의 참가 관계를 조회한 뒤 그룹과 반개구간 조건에 맞는 활성 일정을 조회합니다.
     *
     * @param memberId 회원 식별자
     * @param groupIds 그룹 식별자 목록
     * @param startInclusive 조회 시작 시각
     * @param endExclusive 조회 종료 시각
     * @return 참가 일정 목록
     */
    override fun loadActiveSchedules(
        memberId: String,
        groupIds: List<String>,
        startInclusive: Instant,
        endExclusive: Instant
    ): List<CalendarStudyScheduleDto> {
        if (groupIds.isEmpty()) {
            return emptyList()
        }

        val participatedScheduleIds = mongoTemplate.find(
            Query(
                Criteria.where("memberId").`is`(ObjectId(memberId))
                    .and("isDeleted").ne(true)
            ),
            ScheduleParticipant::class.java
        ).map { it.scheduleId }.distinct()
        if (participatedScheduleIds.isEmpty()) {
            return emptyList()
        }

        val criteria = Criteria.where("groupId").`in`(groupIds.map(::ObjectId))
            .and("_id").`in`(participatedScheduleIds)
            .and("scheduleAt").gte(startInclusive).lt(endExclusive)
            .and("isDeleted").`is`(false)
        return mongoTemplate.find(
            Query(criteria),
            StudySchedule::class.java
        ).map { schedule ->
            CalendarStudyScheduleDto(
                id = schedule.identifier,
                groupId = schedule.groupId.toHexString(),
                title = schedule.title,
                eventAt = schedule.scheduleAt,
                location = LocationVo.from(schedule.location)
            )
        }
    }
}
