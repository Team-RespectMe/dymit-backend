package net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudyScheduleProfileImageType
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleRole
import org.bson.types.ObjectId

class ScheduleRoleDto(
    val memberId: String = "",
    val nickname: String = "",
    val image: ProfileImageVo = ProfileImageVo(type = StudyScheduleProfileImageType.PRESET, url = "0"),
    val roles : List<String>,
    val color: String = "#FF3357"
) {

    fun toDomain(): ScheduleRole {
        return ScheduleRole(
            memberId = ObjectId(memberId),
            nickname = nickname,
            image = image,
            roles = roles,
            color = color
        )
    }

    companion object {

        fun from(role: ScheduleRole) : ScheduleRoleDto {
            return ScheduleRoleDto(
                memberId = role.memberId.toHexString(),
                nickname = role.nickname,
                image = role.image,
                roles = role.roles,
                color = role.color
            )
        }
    }
}