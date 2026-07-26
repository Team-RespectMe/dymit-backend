package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import org.bson.types.ObjectId
import java.time.LocalDateTime

enum class StudyScheduleGroupMemberRoleDto {
    OWNER,
    ADMIN,
    MEMBER
}

data class StudyScheduleGroupProfileImageDto(
    val type: ProfileImageType = ProfileImageType.PRESET,
    val url: String = ""
)

data class StudyScheduleGroupImageDto(
    val type: ProfileImageType = ProfileImageType.PRESET,
    val original: String = "",
    val thumbnail: String = ""
)

data class StudyScheduleGroupDto(
    val id: ObjectId? = null,
    val ownerId: ObjectId = ObjectId.get(),
    val name: String = "",
    val description: String = "",
    val profileImage: StudyScheduleGroupImageDto = StudyScheduleGroupImageDto(),
    val memberCount: Int = 0,
    val createdAt: LocalDateTime? = null
) {

    val identifier: String
        get() = id!!.toHexString()
}

data class StudyScheduleGroupMemberDto(
    val id: ObjectId? = null,
    val groupId: ObjectId = ObjectId.get(),
    val memberId: ObjectId = ObjectId.get(),
    val nickname: String = "",
    val profileImage: StudyScheduleGroupProfileImageDto = StudyScheduleGroupProfileImageDto(),
    val role: StudyScheduleGroupMemberRoleDto = StudyScheduleGroupMemberRoleDto.MEMBER,
    val createdAt: LocalDateTime? = null,
    val isDeleted: Boolean = false
)
