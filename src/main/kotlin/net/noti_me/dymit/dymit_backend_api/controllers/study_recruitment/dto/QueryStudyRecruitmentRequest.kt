package net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment.dto

import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.dto.QueryStudyRecruitmentQuery

/**
 * 스터디 모집 목록 조회 요청 DTO입니다.
 *
 * @property cursor 다음 페이지 조회를 위한 커서
 * @property size 조회 개수
 */
data class QueryStudyRecruitmentRequest(
    val cursor: String? = null,
    val size: Int = 20
) {

    /**
     * 서비스 레이어 조회 쿼리 DTO로 변환합니다.
     *
     * @return QueryStudyRecruitmentQuery
     */
    fun toQuery(): QueryStudyRecruitmentQuery {
        return QueryStudyRecruitmentQuery(
            cursor = cursor,
            size = size
        )
    }
}
