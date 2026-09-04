package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import java.time.Instant

/**
 * 출력 포트가 제공하는 영속 스터디 모집 정보입니다.
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
data class StudyRecruitmentPersistenceDto(
    val id: String,
    val externalId: String,
    val type: StudyRecruitmentType,
    val title: String,
    val content: String,
    val url: String,
    val writer: String,
    val createdAt: Instant?,
    val updatedAt: Instant?
)
