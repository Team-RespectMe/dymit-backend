package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentSummaryDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import java.time.LocalDateTime

/**
 * Dymit 스터디 모집글 목록 요약 응답입니다.
 *
 * @property id 모집글 식별자
 * @property createdAt 생성 시각
 * @property title 모집글 제목
 * @property purpose 스터디 목적
 * @property writerId 작성자 식별자
 * @property tags 태그 목록
 * @property type 모집글 출처 유형
 * @property status 모집 상태
 */
data class DymitStudyRecruitmentSummaryResponse(
    val id: String,
    val createdAt: LocalDateTime?,
    val title: String,
    val purpose: String,
    val writerId: String,
    val tags: List<String>,
    val type: StudyRecruitmentType,
    val status: DymitStudyRecruitmentStatus
) {

    companion object {

        /**
         * 입력 포트 요약 DTO를 웹 응답으로 변환합니다.
         *
         * @param recruitment 모집글 목록 요약 DTO
         * @return 모집글 목록 요약 응답
         */
        fun from(
            recruitment: DymitStudyRecruitmentSummaryDto
        ): DymitStudyRecruitmentSummaryResponse {
            return DymitStudyRecruitmentSummaryResponse(
                id = recruitment.id,
                createdAt = recruitment.createdAt,
                title = recruitment.title,
                purpose = recruitment.purpose,
                writerId = recruitment.writerId,
                tags = recruitment.tags,
                type = recruitment.type,
                status = recruitment.status
            )
        }
    }
}
