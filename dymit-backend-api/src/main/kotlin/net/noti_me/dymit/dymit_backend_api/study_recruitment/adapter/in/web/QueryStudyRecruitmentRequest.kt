package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.web

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.QueryStudyRecruitmentCommand

/**
 * 스터디 모집 목록 조회 요청입니다.
 *
 * @property cursor 다음 페이지 조회를 위한 커서
 * @property size 조회 개수
 */
data class QueryStudyRecruitmentRequest(
    val cursor: String? = null,
    val size: Int = 20
) {

    /**
     * 입력 포트 명령으로 변환합니다.
     *
     * @return 스터디 모집 목록 조회 명령
     */
    fun toCommand(): QueryStudyRecruitmentCommand {
        return QueryStudyRecruitmentCommand(
            cursor = cursor,
            size = size
        )
    }
}
