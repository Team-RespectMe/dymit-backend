package net.noti_me.dymit.dymit_backend_api.application.study_recruitment.usecases

import net.noti_me.dymit.dymit_backend_api.application.study_recruitment.dto.QueryStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment

/**
 * 스터디 모집 목록 조회 유즈케이스 인터페이스입니다.
 */
interface QueryStudyRecruitmentUseCase {

    /**
     * 스터디 모집 목록을 조회합니다.
     *
     * @param query 스터디 모집 목록 조회 Query DTO
     * @return 스터디 모집 도메인 엔티티 목록
     */
    fun queryStudyRecruitments(query: QueryStudyRecruitmentQuery): List<StudyRecruitment>
}
