package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule

import org.bson.types.ObjectId

/**
 * 스터디 일정 첨부 파일의 다중 연결 여부를 조회하는 영속성 포트입니다.
 */
interface ScheduleAttachmentLinkQueryRepository {

    /**
     * 특정 스케줄을 제외하고 다른 스케줄에 여전히 연결된 파일 ID 목록을 조회합니다.
     *
     * @param fileIds 조회할 파일 ID 목록
     * @param scheduleId 제외할 스케줄 ID
     * @return 다른 스케줄에 연결된 파일 ID 집합
     */
    fun findAttachedFileIdsExcludingSchedule(
        fileIds: List<ObjectId>,
        scheduleId: ObjectId
    ): Set<ObjectId>
}
