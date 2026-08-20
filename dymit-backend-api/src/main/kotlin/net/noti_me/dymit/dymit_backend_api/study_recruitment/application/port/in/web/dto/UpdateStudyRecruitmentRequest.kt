package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Size
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.UpdateDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import java.time.Instant

/**
 * Dymit 스터디 모집글 수정 요청입니다.
 *
 * @property description 스터디 소개
 * @property purpose 스터디 목적
 * @property targetMember 모집 대상
 * @property studyFormat 운영 방식
 * @property contact 연락처 또는 연락 URL
 * @property recruitmentStart 모집 시작 시각
 * @property recruitmentEnd 모집 종료 시각
 * @property status 모집 상태
 * @property tags 태그 목록
 */
data class UpdateStudyRecruitmentRequest(
    @field:Size(max = 200)
    val description: String,
    @field:Size(max = 50)
    val purpose: String,
    @field:Size(max = 100)
    val targetMember: String,
    @field:Size(max = 100)
    val studyFormat: String,
    @field:Size(max = 255)
    val contact: String,
    val recruitmentStart: Instant? = null,
    val recruitmentEnd: Instant? = null,
    val status: DymitStudyRecruitmentStatus,
    val tags: List<String> = emptyList()
) {

    /**
     * 웹 요청을 수정 명령으로 변환합니다.
     *
     * @param recruitmentId 수정할 모집글 식별자
     * @return Dymit 스터디 모집글 수정 명령
     */
    fun toCommand(recruitmentId: String): UpdateDymitStudyRecruitmentCommand {
        return UpdateDymitStudyRecruitmentCommand(
            recruitmentId = recruitmentId,
            description = description,
            purpose = purpose,
            targetMember = targetMember,
            studyFormat = studyFormat,
            contact = contact,
            recruitmentStart = recruitmentStart,
            recruitmentEnd = recruitmentEnd,
            status = status,
            tags = tags
        )
    }
}
