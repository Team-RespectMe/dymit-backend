package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

/**
 * Dymit 스터디 모집글 끌어올리기 명령입니다.
 *
 * @property recruitmentId 모집글 식별자
 */
data class BumpStudyRecruitmentCommand(
    val recruitmentId: String
)
