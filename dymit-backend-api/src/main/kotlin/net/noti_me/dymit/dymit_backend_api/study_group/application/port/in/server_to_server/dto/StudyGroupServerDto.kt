package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType
import org.bson.types.ObjectId
import java.time.Instant

enum class StudyGroupMemberRoleDto {
    OWNER,
    ADMIN,
    MEMBER,
}

data class StudyGroupProfileImageDto(
    val type: StudyGroupProfileImageType = StudyGroupProfileImageType.PRESET,
    val url: String = ""
)

data class StudyGroupImageDto(
    val type: StudyGroupProfileImageType = StudyGroupProfileImageType.PRESET,
    val original: String = "",
    val thumbnail: String = ""
) {
    companion object {
        /**
         * 외부 모듈의 프로필 이미지 값을 스터디 그룹 이미지 DTO로 변환합니다.
         */
        fun of(
            type: String,
            original: String,
            thumbnail: String
        ): StudyGroupImageDto {
            return StudyGroupImageDto(
                type = StudyGroupProfileImageType.valueOf(type),
                original = original,
                thumbnail = thumbnail
            )
        }
    }
}

data class StudyGroupRecentPostDto(
    val postId: String,
    val title: String,
    val createdAt: Instant
)

data class StudyGroupRecentScheduleDto(
    val scheduleId: ObjectId,
    val title: String,
    val session: Long,
    val scheduleAt: Instant
)

data class StudyGroupDto(
    val id: ObjectId? = null,
    val ownerId: ObjectId = ObjectId.get(),
    val name: String = "",
    val description: String = "",
    val profileImage: StudyGroupImageDto = StudyGroupImageDto(),
    val memberCount: Int = 0,
    var recentPost: StudyGroupRecentPostDto? = null,
    var recentSchedule: StudyGroupRecentScheduleDto? = null,
    val createdAt: Instant? = null
) {

    val identifier: String
        get() = id!!.toHexString()

    fun updateRecentPost(recentPost: StudyGroupRecentPostDto?) {
        this.recentPost = recentPost
    }

    fun updateRecentSchedule(recentSchedule: StudyGroupRecentScheduleDto?) {
        this.recentSchedule = recentSchedule
    }
}

data class StudyGroupMemberDto(
    val id: ObjectId? = null,
    val groupId: ObjectId = ObjectId.get(),
    val memberId: ObjectId = ObjectId.get(),
    val nickname: String = "",
    val profileImage: StudyGroupProfileImageDto = StudyGroupProfileImageDto(),
    val role: StudyGroupMemberRoleDto = StudyGroupMemberRoleDto.MEMBER,
    val createdAt: Instant? = null,
    val isDeleted: Boolean = false
)
