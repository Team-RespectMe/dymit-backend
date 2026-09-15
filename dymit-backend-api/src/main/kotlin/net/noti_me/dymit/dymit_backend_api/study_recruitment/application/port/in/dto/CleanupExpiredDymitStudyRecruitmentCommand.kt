package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

import java.time.Instant

/**
 * 만료된 Dymit 스터디 모집글 정리 명령입니다.
 *
 * @property cutoff 삭제 대상에 포함되는 마지막 수정 시각
 */
data class CleanupExpiredDymitStudyRecruitmentCommand(
    val cutoff: Instant
)
