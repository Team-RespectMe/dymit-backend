package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics

import org.bson.types.ObjectId
import java.time.Instant

/**
 * 과제 통계 원천을 조회하는 포트입니다.
 */
interface TaskStatisticsSourcePort {

    /**
     * 조회 범위에서 변경되었거나 새로 마감된 과제 원천을 반환합니다.
     */
    fun loadChanged(query: StatisticsSourceQuery): StatisticsSourceChanges<TaskStatisticsSourceData>

    /**
     * 가입 이후의 전체 과제 원천을 반환합니다.
     */
    fun loadAll(query: StatisticsSourceQuery): List<TaskStatisticsSourceData>
}

/**
 * 일정 통계 원천을 조회하는 포트입니다.
 */
interface ScheduleStatisticsSourcePort {

    /** 그룹별 진행 회차를 일정 시각과 ID 순으로 반환합니다. */
    fun loadBoundaries(
        groupIds: List<ObjectId>,
        observedAt: Instant
    ): Map<ObjectId, List<StatisticsSessionBoundary>>

    /** 삭제되지 않은 일정이 하나 이상 있는 그룹 ID를 반환합니다. */
    fun loadGroupIdsHavingSchedule(groupIds: List<ObjectId>): Set<ObjectId>

    /**
     * 조회 범위에서 변경되었거나 새로 시작된 일정 원천을 반환합니다.
     */
    fun loadChanged(query: StatisticsSourceQuery): StatisticsSourceChanges<ScheduleStatisticsSourceData>

    /**
     * 가입 이후의 전체 일정 원천을 반환합니다.
     */
    fun loadAll(query: StatisticsSourceQuery): List<ScheduleStatisticsSourceData>
}
