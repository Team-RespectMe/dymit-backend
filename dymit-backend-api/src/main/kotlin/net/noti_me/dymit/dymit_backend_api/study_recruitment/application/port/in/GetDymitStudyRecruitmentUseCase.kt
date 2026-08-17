package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentQuery

/**
 * Dymit 스터디 모집글 단건 조회 유즈케이스입니다.
 */
interface GetDymitStudyRecruitmentUseCase {

    /**
     * Dymit 스터디 모집글을 단건 조회합니다.
     *
     * @param query 단건 조회 쿼리
     * @return 모집글 DTO
     */
    fun execute(query: GetDymitStudyRecruitmentQuery): DymitStudyRecruitmentDto
}
