package net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule

class StudyScheduleParticipantDto(
    val scheduleId: String,
    val memberId: String,
    val nickname: String,
    val image: ProfileImageVo
) {

    companion object {

        fun of(
            schedule: StudySchedule,
            member: StudyGroupMember
        ): StudyScheduleParticipantDto {
            return StudyScheduleParticipantDto(
                scheduleId = schedule.identifier,
                memberId = member.memberId.toHexString(),
                nickname = member.nickname,
                image = ProfileImageVo(
                    type = member.profileImage.type,
                    url = member.profileImage.url
                )
            )
        }
    }
}