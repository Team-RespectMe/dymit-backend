package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType

data class StudyGroupMemberEventDto(
    val memberId: String,
    val nickname: String,
    val roles: List<String>,
    val profileImageType: StudyGroupProfileImageType = StudyGroupProfileImageType.PRESET,
    val profileImageUrl: String = ""
) {
    companion object {
        /**
         * 외부 모듈의 프로필 이미지 값을 스터디 그룹 이벤트 DTO로 변환합니다.
         */
        fun of(
            memberId: String,
            nickname: String,
            roles: List<String>,
            profileImageType: String,
            profileImageUrl: String
        ): StudyGroupMemberEventDto {
            return StudyGroupMemberEventDto(
                memberId = memberId,
                nickname = nickname,
                roles = roles,
                profileImageType = StudyGroupProfileImageType.valueOf(profileImageType),
                profileImageUrl = profileImageUrl
            )
        }
    }
}
