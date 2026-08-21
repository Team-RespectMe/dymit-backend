package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import java.time.LocalDateTime

/**
 * Dymit 스터디 모집글 목록 요약 DTO입니다.
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
data class DymitStudyRecruitmentSummaryDto(
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
         * 영속성 DTO를 목록 요약 DTO로 변환합니다.
         *
         * @param recruitment 영속성 계층 모집글 DTO
         * @return 모집글 목록 요약 DTO
         */
        fun from(
            recruitment: DymitStudyRecruitmentPersistenceDto
        ): DymitStudyRecruitmentSummaryDto {
            return DymitStudyRecruitmentSummaryDto(
                id = recruitment.id.toHexString(),
                createdAt = recruitment.createdAt,
                title = recruitment.title,
                purpose = recruitment.purpose,
                writerId = recruitment.writerId.toHexString(),
                tags = recruitment.tags,
                type = recruitment.type,
                status = recruitment.recruitmentStatus
            )
        }
    }
}
