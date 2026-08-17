package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

/**
 * Dymit 스터디 모집글 삭제 명령입니다.
 *
 * @property recruitmentId 모집글 식별자
 */
data class DeleteDymitStudyRecruitmentCommand(
    val recruitmentId: String
)
