package net.noti_me.dymit.dymit_backend_api.calendar.adapter.out.task

import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task.LoadCalendarTaskPort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task.dto.CalendarTaskDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssignee
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * 과제와 제출 대상 및 관련 일정 문서에서 캘린더 과제를 조회하는 어댑터입니다.
 *
 * @property mongoTemplate MongoDB 조회 도구
 */
@Repository
class MongoCalendarTaskAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadCalendarTaskPort {

    /**
     * 회원의 활성 제출 대상 과제와 활성 관련 일정을 일괄 조회합니다.
     *
     * @param memberId 회원 식별자
     * @param groupIds 현재 소속 그룹 식별자 목록
     * @param startInclusive 조회 시작 시각
     * @param endExclusive 조회 종료 시각
     * @return 제출 대상 과제 목록
     */
    override fun loadActiveTasks(
        memberId: String,
        groupIds: List<String>,
        startInclusive: Instant,
        endExclusive: Instant
    ): List<CalendarTaskDto> {
        if (groupIds.isEmpty()) {
            return emptyList()
        }

        val assignedTaskIds = mongoTemplate.find(
            Query(
                Criteria.where("memberId").`is`(ObjectId(memberId))
                    .and("isDeleted").`is`(false)
            ),
            TaskAssignee::class.java
        ).map { it.taskId }.distinct()
        if (assignedTaskIds.isEmpty()) {
            return emptyList()
        }

        val tasks = mongoTemplate.find(
            Query(
                Criteria.where("_id").`in`(assignedTaskIds)
                    .and("expireAt").gte(startInclusive).lt(endExclusive)
                    .and("isDeleted").`is`(false)
            ),
            Task::class.java
        )
        if (tasks.isEmpty()) {
            return emptyList()
        }

        val schedulesById = mongoTemplate.find(
            Query(
                Criteria.where("_id").`in`(tasks.map { it.relatedScheduleId }.distinct())
                    .and("groupId").`in`(groupIds.map(::ObjectId))
                    .and("isDeleted").`is`(false)
            ),
            StudySchedule::class.java
        ).associateBy { it.id }

        return tasks.mapNotNull { task ->
            schedulesById[task.relatedScheduleId]?.let { schedule ->
                CalendarTaskDto(
                    id = task.identifier,
                    groupId = schedule.groupId.toHexString(),
                    title = task.title,
                    eventAt = task.expireAt,
                    submissionType = task.submissionType
                )
            }
        }
    }
}
