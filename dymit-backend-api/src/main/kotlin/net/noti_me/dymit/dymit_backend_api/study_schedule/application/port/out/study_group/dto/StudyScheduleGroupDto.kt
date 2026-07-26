package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudyScheduleProfileImageType
import org.bson.types.ObjectId
import java.time.LocalDateTime

enum class StudyScheduleGroupMemberRoleDto {
    OWNER,
    ADMIN,
    MEMBER
}

data class StudyScheduleGroupProfileImageDto(
    val type: StudyScheduleProfileImageType = StudyScheduleProfileImageType.PRESET,
    val url: String = ""
) {
    companion object {
        /**
         * 외부 모듈의 프로필 이미지 값을 일정 모듈 DTO로 변환합니다.
         */
        fun of(type: String, url: String): StudyScheduleGroupProfileImageDto {
            return StudyScheduleGroupProfileImageDto(
                type = StudyScheduleProfileImageType.valueOf(type),
                url = url
            )
        }
    }
}

data class StudyScheduleGroupImageDto(
    val type: StudyScheduleProfileImageType = StudyScheduleProfileImageType.PRESET,
    val original: String = "",
    val thumbnail: String = ""
) {
    companion object {
        /**
         * 외부 모듈의 그룹 이미지 값을 일정 모듈 DTO로 변환합니다.
         */
        fun of(
            type: String,
            original: String,
            thumbnail: String
        ): StudyScheduleGroupImageDto {
            return StudyScheduleGroupImageDto(
                type = StudyScheduleProfileImageType.valueOf(type),
                original = original,
                thumbnail = thumbnail
            )
        }
    }
}

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
