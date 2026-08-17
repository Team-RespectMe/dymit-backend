package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

/**
 * Dymit 스터디 모집글 단건 조회 쿼리입니다.
 *
 * @property recruitmentId 모집글 식별자
 */
data class GetDymitStudyRecruitmentQuery(
    val recruitmentId: String
)
