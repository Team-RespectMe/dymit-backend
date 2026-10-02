package net.noti_me.dymit.dymit_backend_api.calendar.adapter.out.study_group

import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group.LoadCalendarStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group.dto.CalendarStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * 스터디 그룹 문서에서 캘린더용 현재 소속 그룹을 조회하는 어댑터입니다.
 *
 * @property mongoTemplate MongoDB 조회 도구
 */
@Repository
class MongoCalendarStudyGroupAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadCalendarStudyGroupPort {

    /**
     * 삭제되지 않은 소속 관계와 그룹을 한 번씩 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return 현재 소속 그룹 목록
     */
    override fun loadActiveGroupsByMemberId(memberId: String): List<CalendarStudyGroupDto> {
        val memberships = mongoTemplate.find(
            Query(
                Criteria.where("memberId").`is`(ObjectId(memberId))
                    .and("isDeleted").`is`(false)
            ),
            StudyGroupMember::class.java
        )
        val groupIds = memberships.map { it.groupId }.distinct()
        if (groupIds.isEmpty()) {
            return emptyList()
        }

        return mongoTemplate.find(
            Query(
                Criteria.where("_id").`in`(groupIds)
                    .and("isDeleted").`is`(false)
            ),
            StudyGroup::class.java
        ).map { group ->
            CalendarStudyGroupDto(
                groupId = group.identifier,
                groupName = group.name
            )
        }
    }
}
