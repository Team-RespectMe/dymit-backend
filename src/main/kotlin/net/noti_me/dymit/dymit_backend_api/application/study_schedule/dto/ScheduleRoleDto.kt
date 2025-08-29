package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import org.bson.types.ObjectId

class ScheduleRoleDto(
    val memberId: String = "",
    val nickname: String = "",
    val image: ProfileImageVo = ProfileImageVo(type = "preset", url = "0"),
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