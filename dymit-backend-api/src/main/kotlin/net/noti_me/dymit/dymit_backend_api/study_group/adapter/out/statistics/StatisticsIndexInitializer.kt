package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.statistics

import jakarta.annotation.PostConstruct
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupWeeklyStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberWeeklyStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.stereotype.Component

/**
 * 통계 CAS와 주간 조회에 필요한 MongoDB 인덱스를 보장합니다.
 */
@Component
class StatisticsIndexInitializer(
    private val mongoTemplate: MongoTemplate
) {

    /**
     * 애플리케이션 시작 시 통계 인덱스를 생성합니다.
     */
    @PostConstruct
    fun ensureIndexes() {
        mongoTemplate.indexOps(MemberStatisticsLedger::class.java).ensureIndex(
            Index().on("membershipId", Sort.Direction.ASC).unique()
                .named("member_statistics_membership_uq")
        )
        mongoTemplate.indexOps(MemberWeeklyStatistics::class.java).ensureIndex(
            CompoundIndexDefinition(
                org.bson.Document(mapOf("membershipId" to 1, "weekEnd" to 1))
            ).unique().named("member_weekly_statistics_membership_week_uq")
        )
        mongoTemplate.indexOps(MemberWeeklyStatistics::class.java).ensureIndex(
            CompoundIndexDefinition(
                org.bson.Document(mapOf("groupId" to 1, "weekEnd" to -1, "membershipId" to 1))
            ).named("member_weekly_statistics_group_week_idx")
        )
        mongoTemplate.indexOps(GroupWeeklyStatistics::class.java).ensureIndex(
            CompoundIndexDefinition(
                org.bson.Document(mapOf("groupId" to 1, "weekEnd" to 1))
            ).unique().named("group_weekly_statistics_group_week_uq")
        )
        ensureSourceIndexes()
    }

    private fun ensureSourceIndexes() {
        mongoTemplate.indexOps("tasks").ensureIndex(compound(
            "statistics_task_schedule_expire_idx",
            "relatedScheduleId" to 1,
            "expireAt" to 1
        ))
        mongoTemplate.indexOps("tasks").ensureIndex(compound(
            "statistics_task_schedule_updated_idx",
            "relatedScheduleId" to 1,
            "updatedAt" to 1
        ))
        mongoTemplate.indexOps("task_assignees").ensureIndex(compound(
            "statistics_assignee_member_updated_idx",
            "memberId" to 1,
            "updatedAt" to 1
        ))
        mongoTemplate.indexOps("task_assignees").ensureIndex(compound(
            "statistics_assignee_member_created_idx",
            "memberId" to 1,
            "createdAt" to 1
        ))
        mongoTemplate.indexOps("task_assignees").ensureIndex(compound(
            "statistics_assignee_task_member_idx",
            "taskId" to 1,
            "memberId" to 1
        ))
        mongoTemplate.indexOps("study_schedules").ensureIndex(compound(
            "statistics_schedule_group_time_idx",
            "groupId" to 1,
            "scheduleAt" to 1
        ))
        mongoTemplate.indexOps("study_schedules").ensureIndex(compound(
            "statistics_schedule_group_updated_idx",
            "groupId" to 1,
            "updatedAt" to 1
        ))
        mongoTemplate.indexOps("study_schedules").ensureIndex(compound(
            "statistics_schedule_group_created_idx",
            "groupId" to 1,
            "createdAt" to 1
        ))
        mongoTemplate.indexOps("study_schedule_participants").ensureIndex(compound(
            "statistics_participant_member_updated_idx",
            "memberId" to 1,
            "updatedAt" to 1
        ))
        mongoTemplate.indexOps("study_schedule_participants").ensureIndex(compound(
            "statistics_participant_schedule_member_idx",
            "scheduleId" to 1,
            "memberId" to 1
        ))
        mongoTemplate.indexOps(StudyGroupMember::class.java).ensureIndex(compound(
            "statistics_membership_group_active_id_idx",
            "groupId" to 1,
            "isDeleted" to 1,
            "_id" to 1
        ))
        mongoTemplate.indexOps(StudyGroupMember::class.java).ensureIndex(compound(
            "statistics_membership_group_member_active_idx",
            "groupId" to 1,
            "memberId" to 1,
            "isDeleted" to 1
        ))
    }

    private fun compound(name: String, vararg fields: Pair<String, Int>): IndexDefinition {
        return CompoundIndexDefinition(org.bson.Document(fields.toMap())).named(name)
    }
}
