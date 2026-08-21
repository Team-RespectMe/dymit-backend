package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto

import jakarta.validation.constraints.Size
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CreateDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import java.time.Instant

/**
 * Dymit 스터디 모집글 생성 요청입니다.
 *
 * @property groupId 모집 대상 그룹 식별자
 * @property title 모집글 제목
 * @property description 스터디 소개
 * @property purpose 스터디 목적
 * @property targetMember 모집 대상
 * @property studyFormat 운영 방식
 * @property contact 연락처 정보
 * @property recruitmentStart 모집 시작 시각
 * @property recruitmentEnd 모집 종료 시각
 * @property tags 태그 목록
 */
data class CreateStudyRecruitmentRequest(
    val groupId: String,
    @field:Size(max = 50)
    val title: String,
    @field:Size(max = 200)
    val description: String,
    @field:Size(max = 50)
    val purpose: String,
    @field:Size(max = 100)
    val targetMember: String,
    @field:Size(max = 100)
    val studyFormat: String,
    val contact: Contact,
    val recruitmentStart: Instant? = null,
    val recruitmentEnd: Instant? = null,
    val tags: List<String> = emptyList()
) {

    /**
     * 웹 요청을 생성 명령으로 변환합니다.
     *
     * @return Dymit 스터디 모집글 생성 명령
     */
    fun toCommand(): CreateDymitStudyRecruitmentCommand {
        return CreateDymitStudyRecruitmentCommand(
            groupId = groupId,
            title = title,
            description = description,
            purpose = purpose,
            targetMember = targetMember,
            studyFormat = studyFormat,
            contact = contact,
            recruitmentStart = recruitmentStart,
            recruitmentEnd = recruitmentEnd,
            tags = tags
        )
    }
}
