package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule

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