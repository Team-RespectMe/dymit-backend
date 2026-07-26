package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

/**
 * 스터디 모집 목록 조회 명령입니다.
 *
 * @property cursor 다음 페이지 조회를 위한 커서
 * @property size 응답할 모집글 개수
 */
data class QueryStudyRecruitmentCommand(
    val cursor: String? = null,
    val size: Int = 20
)
