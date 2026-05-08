package net.noti_me.dymit.dymit_backend_api.application.study_recruitment.dto

/**
 * 스터디 모집 목록 조회 Query DTO입니다.
 *
 * @property cursor 다음 페이지 조회를 위한 커서
 * @property size 조회 개수
 */
data class QueryStudyRecruitmentQuery(
    val cursor: String? = null,
    val size: Int = 20
)
