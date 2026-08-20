package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import java.time.Instant
import java.time.LocalDateTime

/**
 * Dymit 스터디 모집글 웹 응답입니다.
 *
 * @property id 모집글 식별자
 * @property writer 작성자 정보
 * @property groupId 그룹 식별자
 * @property type 모집글 출처 유형
 * @property title 모집글 제목
 * @property description 스터디 소개
 * @property purpose 스터디 목적
 * @property recruitmentStatus 모집 상태
 * @property recruitmentStart 모집 시작 시각
 * @property recruitmentEnd 모집 종료 시각
 * @property targetMember 모집 대상
 * @property studyFormat 운영 방식
 * @property contact 연락처 또는 연락 URL
 * @property tags 태그 목록
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 */
data class DymitStudyRecruitmentResponse(
    val id: String,
    val writer: DymitStudyRecruitmentWriterResponse,
    val groupId: String,
    val type: StudyRecruitmentType,
    val title: String,
    val description: String,
    val purpose: String,
    val recruitmentStatus: DymitStudyRecruitmentStatus,
    val recruitmentStart: Instant?,
    val recruitmentEnd: Instant?,
    val targetMember: String,
    val studyFormat: String,
    val contact: String,
    val tags: List<String>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) : BaseResponse() {

    companion object {

        /**
         * 입력 포트 DTO를 웹 응답으로 변환합니다.
         *
         * @param recruitment Dymit 스터디 모집글 DTO
         * @return 웹 응답
         */
        fun from(recruitment: DymitStudyRecruitmentDto): DymitStudyRecruitmentResponse {
            return DymitStudyRecruitmentResponse(
                id = recruitment.id,
                writer = DymitStudyRecruitmentWriterResponse.from(recruitment),
                groupId = recruitment.groupId,
                type = recruitment.type,
                title = recruitment.title,
                description = recruitment.description,
                purpose = recruitment.purpose,
                recruitmentStatus = recruitment.recruitmentStatus,
                recruitmentStart = recruitment.recruitmentStart,
                recruitmentEnd = recruitment.recruitmentEnd,
                targetMember = recruitment.targetMember,
                studyFormat = recruitment.studyFormat,
                contact = recruitment.contact,
                tags = recruitment.tags,
                createdAt = recruitment.createdAt,
                updatedAt = recruitment.updatedAt
            )
        }
    }
}
