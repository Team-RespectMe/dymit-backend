package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

/**
 * Dymit 스터디 모집글 목록 조회 쿼리입니다.
 *
 * @property cursor 다음 페이지 조회를 위한 커서
 * @property size 응답할 모집글 개수
 * @property mine 로그인 회원이 작성한 모집글만 조회할지 여부
 * @property memberId 요청 회원 식별자
 */
data class GetDymitStudyRecruitmentListQuery(
    val cursor: String? = null,
    val size: Int = 20,
    val mine: Boolean = false,
    val memberId: String = ""
)
