package net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment

import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.dto.QueryStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.usecases.QueryStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import org.springframework.stereotype.Service

/**
 * 스터디 모집 조회 비즈니스 로직을 조합하는 서비스 파사드입니다.
 *
 * @property queryStudyRecruitmentUseCase 스터디 모집 조회 유즈케이스
 */
@Service
class StudyRecruitmentServiceFacade(
    private val queryStudyRecruitmentUseCase: QueryStudyRecruitmentUseCase
) {

    /**
     * 스터디 모집 목록을 조회합니다.
     *
     * @param query 스터디 모집 목록 조회 쿼리
     * @return 스터디 모집 도메인 엔티티 목록
     */
    fun getStudyRecruitments(query: QueryStudyRecruitmentQuery): List<StudyRecruitment> {
        val fetchQuery = query.copy(size = query.size + 1)
        return queryStudyRecruitmentUseCase.queryStudyRecruitments(fetchQuery)
    }
}
