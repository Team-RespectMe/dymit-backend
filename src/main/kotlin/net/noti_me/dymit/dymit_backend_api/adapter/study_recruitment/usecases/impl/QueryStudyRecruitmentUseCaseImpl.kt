package net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.usecases.impl

import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.dto.QueryStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.usecases.QueryStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_recruitment.StudyRecruitmentRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 스터디 모집 목록 조회 유즈케이스 구현체입니다.
 *
 * @property studyRecruitmentRepository 스터디 모집 영속성 포트
 */
@Service
class QueryStudyRecruitmentUseCaseImpl(
    private val studyRecruitmentRepository: StudyRecruitmentRepository
) : QueryStudyRecruitmentUseCase {

    /**
     * 커서 기반으로 스터디 모집 목록을 조회합니다.
     *
     * @param query 스터디 모집 목록 조회 Query DTO
     * @return 스터디 모집 도메인 엔티티 목록
     */
    override fun queryStudyRecruitments(query: QueryStudyRecruitmentQuery): List<StudyRecruitment> {
        val cursorId = query.cursor?.let { ObjectId(it) }
        return studyRecruitmentRepository.findByCursorOrderByIdDesc(cursorId, query.size)
    }
}
