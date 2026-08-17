package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentSummaryDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentListQuery

/**
 * Dymit 스터디 모집글 목록 조회 유즈케이스입니다.
 */
interface GetDymitStudyRecruitmentListUseCase {

    /**
     * Dymit 스터디 모집글 목록을 커서 기반으로 조회합니다.
     *
     * @param query 목록 조회 쿼리
     * @return 다음 페이지 판단용 한 건을 포함한 모집글 요약 DTO 목록
     */
    fun execute(query: GetDymitStudyRecruitmentListQuery): List<DymitStudyRecruitmentSummaryDto>
}
