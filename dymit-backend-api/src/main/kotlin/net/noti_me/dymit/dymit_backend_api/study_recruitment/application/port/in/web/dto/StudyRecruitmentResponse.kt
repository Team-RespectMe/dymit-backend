package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.StudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import java.time.LocalDateTime

/**
 * 스터디 모집 목록 응답입니다.
 *
 * @property id 모집글 식별자
 * @property externalId 외부 원본 시스템 식별자
 * @property type 모집글 출처 타입
 * @property title 모집글 제목
 * @property content 모집글 본문
 * @property url 원본 모집글 URL
 * @property writer 원본 작성자명
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 */
data class StudyRecruitmentResponse(
    val id: String,
    val externalId: String,
    val type: StudyRecruitmentType,
    val title: String,
    val content: String,
    val url: String,
    val writer: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {

    companion object {

        /**
         * 입력 포트 DTO를 웹 응답으로 변환합니다.
         *
         * @param recruitment 입력 포트용 스터디 모집 정보
         * @return 스터디 모집 응답
         */
        fun from(recruitment: StudyRecruitmentDto): StudyRecruitmentResponse {
            return StudyRecruitmentResponse(
                id = recruitment.id,
                externalId = recruitment.externalId,
                type = recruitment.type,
                title = recruitment.title,
                content = recruitment.content,
                url = recruitment.url,
                writer = recruitment.writer,
                createdAt = recruitment.createdAt,
                updatedAt = recruitment.updatedAt
            )
        }
    }
}
