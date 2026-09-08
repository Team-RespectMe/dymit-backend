package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

/**
 * 그룹별 Dymit 스터디 모집글 존재 여부 정보입니다.
 *
 * @property groupId 그룹 식별자
 * @property exists 모집글 존재 여부
 */
data class DymitStudyRecruitmentExistenceDto(
    val groupId: String,
    val exists: Boolean
)
