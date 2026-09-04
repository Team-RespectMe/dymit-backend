package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentCursor
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import java.time.LocalDateTime

/**
 * Dymit 스터디 모집글 목록 요약 DTO입니다.
 *
 * @property id 모집글 식별자
 * @property groupId 그룹 식별자
 * @property createdAt 생성 시각
 * @property title 모집글 제목
 * @property purpose 스터디 목적
 * @property writerId 작성자 식별자
 * @property tags 태그 목록
 * @property type 모집글 출처 유형
 * @property status 모집 상태
 * @property content 모집글 요약 내용
 * @property url 외부 원본 모집글 URL
 * @property cursor 다음 페이지 조회용 커서
 */
data class DymitStudyRecruitmentSummaryDto(
    val id: String,
    val groupId: String? = null,
    val createdAt: LocalDateTime?,
    val title: String,
    val purpose: String,
    val writerId: String,
    val tags: List<String>,
    val type: StudyRecruitmentType,
    val status: DymitStudyRecruitmentStatus,
    val content: String = "",
    val url: String? = null,
    val cursor: String = id
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
                groupId = recruitment.groupId.toHexString(),
                createdAt = recruitment.createdAt,
                title = recruitment.title,
                purpose = recruitment.purpose,
                writerId = recruitment.writerId.toHexString(),
                tags = recruitment.tags,
                type = recruitment.type,
                status = recruitment.recruitmentStatus,
                content = recruitment.description.take(CONTENT_MAX_LENGTH),
                url = null,
                cursor = DymitStudyRecruitmentCursor(
                    bumpAt = recruitment.bumpAt,
                    recruitmentId = recruitment.id,
                    hasStoredBumpAt = recruitment.hasStoredBumpAt
                ).encode()
            )
        }

        /**
         * 외부 모집글 DTO를 v2 목록 요약 DTO로 변환합니다.
         *
         * @param recruitment v1 외부 모집글 DTO
         * @return v2 모집글 목록 요약 DTO
         */
        fun from(
            recruitment: StudyRecruitmentDto
        ): DymitStudyRecruitmentSummaryDto {
            return DymitStudyRecruitmentSummaryDto(
                id = recruitment.id,
                groupId = null,
                createdAt = recruitment.createdAt,
                title = recruitment.title,
                purpose = "",
                writerId = "",
                tags = emptyList(),
                type = recruitment.type,
                status = DymitStudyRecruitmentStatus.RECRUITING,
                content = recruitment.content,
                url = recruitment.url,
                cursor = recruitment.id
            )
        }

        private const val CONTENT_MAX_LENGTH = 100
    }
}
