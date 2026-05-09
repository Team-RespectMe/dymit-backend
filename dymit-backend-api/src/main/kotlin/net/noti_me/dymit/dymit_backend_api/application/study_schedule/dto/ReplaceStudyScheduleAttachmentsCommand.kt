package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

/**
 * 스터디 일정 첨부 목록 전체 교체 커맨드입니다.
 *
 * @param scheduleId 대상 스케줄 ID
 * @param fileIds 최종 첨부 파일 ID 목록
 */
data class ReplaceStudyScheduleAttachmentsCommand(
    val scheduleId: String,
    val fileIds: List<String>
)
