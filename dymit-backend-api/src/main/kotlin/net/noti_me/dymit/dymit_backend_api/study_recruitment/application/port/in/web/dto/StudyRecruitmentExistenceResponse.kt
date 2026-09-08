package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentExistenceDto

/**
 * 그룹별 스터디 모집글 존재 여부 웹 응답입니다.
 *
 * @property groupId 그룹 식별자
 * @property exists 모집글 존재 여부
 */
data class StudyRecruitmentExistenceResponse(
    val groupId: String,
    val exists: Boolean
) {

    companion object {

        /**
         * 입력 포트 DTO를 웹 응답으로 변환합니다.
         *
         * @param existence 그룹별 모집글 존재 여부 DTO
         * @return 그룹별 모집글 존재 여부 웹 응답
         */
        fun from(
            existence: DymitStudyRecruitmentExistenceDto
        ): StudyRecruitmentExistenceResponse {
            return StudyRecruitmentExistenceResponse(
                groupId = existence.groupId,
                exists = existence.exists
            )
        }
    }
}
