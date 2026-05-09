package net.noti_me.dymit.dymit_backend_api.application.study_schedule

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ReplaceStudyScheduleAttachmentsCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleAttachmentDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 스터디 일정 첨부파일 관리 서비스 인터페이스입니다.
 */
interface StudyScheduleAttachmentService {

    /**
     * 일정의 첨부 목록 전체를 교체합니다.
     *
     * @param memberInfo 로그인 멤버 정보
     * @param command 첨부 교체 커맨드
     * @return 최종 첨부 파일 목록
     */
    fun replaceAttachments(
        memberInfo: MemberInfo,
        command: ReplaceStudyScheduleAttachmentsCommand
    ): List<StudyScheduleAttachmentDto>

    /**
     * 일정의 현재 첨부 목록을 조회합니다.
     *
     * @param memberInfo 로그인 멤버 정보
     * @param scheduleId 조회할 일정 ID
     * @return 첨부 파일 목록
     */
    fun getAttachments(
        memberInfo: MemberInfo,
        scheduleId: String
    ): List<StudyScheduleAttachmentDto>
}
