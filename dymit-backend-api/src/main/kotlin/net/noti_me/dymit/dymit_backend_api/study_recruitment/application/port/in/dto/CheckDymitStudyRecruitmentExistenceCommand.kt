package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

/**
 * 그룹별 Dymit 스터디 모집글 존재 여부 확인 명령입니다.
 *
 * @property groupIds 확인할 그룹 식별자 목록
 */
data class CheckDymitStudyRecruitmentExistenceCommand(
    val groupIds: List<String>
)
