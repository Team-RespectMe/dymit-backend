package net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment.dto

import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import java.time.LocalDateTime

/**
 * 스터디 모집 목록 응답 DTO입니다.
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
    val type: String,
    val title: String,
    val content: String,
    val url: String,
    val writer: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {

    companion object {

        /**
         * StudyRecruitment 도메인 엔티티를 응답 DTO로 변환합니다.
         *
         * @param recruitment 스터디 모집 도메인 엔티티
         * @return StudyRecruitmentResponse
         */
        fun from(recruitment: StudyRecruitment): StudyRecruitmentResponse {
            return StudyRecruitmentResponse(
                id = recruitment.identifier,
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
