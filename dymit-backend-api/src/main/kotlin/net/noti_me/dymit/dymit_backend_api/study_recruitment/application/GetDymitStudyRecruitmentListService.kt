package net.noti_me.dymit.dymit_backend_api.study_recruitment.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentListUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentSummaryDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentListQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.LoadDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentCursor
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
     * Dymit 모집글 목록을 끌어올리기 최신순으로 조회하며 다음 페이지 판단용 한 건을 더 불러옵니다.
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
        val writerId = query.memberId.takeIf { query.mine }?.let(::parseMemberId)
        val cursorValue = query.cursor
        val recruitments = if ( cursorValue != null && ObjectId.isValid(cursorValue) ) {
            loadRecruitmentPort.loadByCursorOrderByIdDesc(
                cursorId = ObjectId(cursorValue),
                size = query.size + 1,
                writerId = writerId
            )
        } else {
            loadRecruitmentPort.loadByCursorOrderByBumpAtDesc(
                cursor = cursorValue?.let(::parseCursor),
                size = query.size + 1,
                writerId = writerId
            )
        }

        return recruitments.map(DymitStudyRecruitmentSummaryDto::from)
    }

    private fun parseCursor(value: String): DymitStudyRecruitmentCursor {
        return try {
            DymitStudyRecruitmentCursor.decode(value)
        } catch ( exception: IllegalArgumentException ) {
            throw BadRequestException(message = "올바르지 않은 커서입니다.")
        }
    }

    private fun parseMemberId(value: String): ObjectId {
        if ( !ObjectId.isValid(value) ) {
            throw BadRequestException(message = "올바르지 않은 회원 식별자입니다.")
        }
        return ObjectId(value)
    }
}
