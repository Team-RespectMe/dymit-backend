package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics

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

    /**
     * 조회 범위에서 변경되었거나 새로 시작된 일정 원천을 반환합니다.
     */
    fun loadChanged(query: StatisticsSourceQuery): StatisticsSourceChanges<ScheduleStatisticsSourceData>

    /**
     * 가입 이후의 전체 일정 원천을 반환합니다.
     */
    fun loadAll(query: StatisticsSourceQuery): List<ScheduleStatisticsSourceData>
}
