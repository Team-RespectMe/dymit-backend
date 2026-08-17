package net.noti_me.dymit.dymit_backend_api.study_recruitment.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentListUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentSummaryDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentListQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.LoadDymitStudyRecruitmentPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * Dymit 스터디 모집글 목록 조회 서비스입니다.
 *
 * @property loadRecruitmentPort 모집글 조회 출력 포트
 */
@Service
class GetDymitStudyRecruitmentListService(
    private val loadRecruitmentPort: LoadDymitStudyRecruitmentPort
) : GetDymitStudyRecruitmentListUseCase {

    /**
     * Dymit 모집글 목록을 최신순으로 조회하며 다음 페이지 판단용 한 건을 더 불러옵니다.
     *
     * @param query 목록 조회 쿼리
     * @return 모집글 요약 DTO 목록
     */
    override fun execute(
        query: GetDymitStudyRecruitmentListQuery
    ): List<DymitStudyRecruitmentSummaryDto> {
        if ( query.size !in 1..100 ) {
            throw BadRequestException(message = "조회 크기는 1 이상 100 이하여야 합니다.")
        }
        val cursorId = query.cursor?.let(::parseCursor)

        return loadRecruitmentPort.loadByCursorOrderByIdDesc(
            cursorId = cursorId,
            size = query.size + 1
        ).map(DymitStudyRecruitmentSummaryDto::from)
    }

    private fun parseCursor(value: String): ObjectId {
        if ( !ObjectId.isValid(value) ) {
            throw BadRequestException(message = "올바르지 않은 커서입니다.")
        }
        return ObjectId(value)
    }
}
