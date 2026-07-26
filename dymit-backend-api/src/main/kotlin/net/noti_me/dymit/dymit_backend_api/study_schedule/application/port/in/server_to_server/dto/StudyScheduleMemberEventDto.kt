package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudyScheduleProfileImageType

data class StudyScheduleMemberEventDto(
    val memberId: String,
    val nickname: String,
    val profileImageType: StudyScheduleProfileImageType,
    val profileImageUrl: String
) {
    companion object {
        /**
         * 외부 모듈의 프로필 이미지 값을 스터디 일정 이벤트 DTO로 변환합니다.
         */
        fun of(
            memberId: String,
            nickname: String,
            profileImageType: String,
            profileImageUrl: String
        ): StudyScheduleMemberEventDto {
            return StudyScheduleMemberEventDto(
                memberId = memberId,
                nickname = nickname,
                profileImageType = StudyScheduleProfileImageType.valueOf(profileImageType),
                profileImageUrl = profileImageUrl
            )
        }
    }
}
