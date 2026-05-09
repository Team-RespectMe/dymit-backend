package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleAttachment
import org.bson.types.ObjectId

/**
 * 스터디 일정 첨부 관계 영속성 포트입니다.
 */
interface ScheduleAttachmentRepository {

    /**
     * 일정 ID로 현재 첨부 목록을 조회합니다.
     *
     * @param scheduleId 조회할 일정 ID
     * @return 첨부 관계 목록
     */
    fun findByScheduleId(scheduleId: ObjectId): List<ScheduleAttachment>

    /**
     * 일정의 첨부 목록 전체를 교체합니다.
     *
     * @param scheduleId 대상 일정 ID
     * @param attachments 최종 첨부 관계 목록
     * @return 저장된 첨부 관계 목록
     */
    fun replaceByScheduleId(
        scheduleId: ObjectId,
        attachments: List<ScheduleAttachment>
    ): List<ScheduleAttachment>
}
