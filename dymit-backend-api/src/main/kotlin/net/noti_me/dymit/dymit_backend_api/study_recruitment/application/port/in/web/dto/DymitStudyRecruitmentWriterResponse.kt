package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto

/**
 * Dymit 스터디 모집글 작성자 응답입니다.
 *
 * @property id 작성자 식별자
 * @property name 모집글에 저장된 작성자 닉네임
 * @property profileImageUrl 작성자의 최신 프로필 이미지 썸네일 URL
 */
data class DymitStudyRecruitmentWriterResponse(
    val id: String,
    val name: String,
    val profileImageUrl: String
) {

    companion object {

        /**
         * 모집글 DTO의 작성자 정보를 웹 응답으로 변환합니다.
         *
         * @param recruitment Dymit 스터디 모집글 DTO
         * @return 작성자 웹 응답
         */
        fun from(
            recruitment: DymitStudyRecruitmentDto
        ): DymitStudyRecruitmentWriterResponse {
            return DymitStudyRecruitmentWriterResponse(
                id = recruitment.writerId,
                name = recruitment.writerNickname,
                profileImageUrl = recruitment.writerProfileImageUrl
            )
        }
    }
}
