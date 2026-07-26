package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import org.bson.types.ObjectId
import java.time.LocalDateTime

enum class StudyGroupMemberRoleDto {
    OWNER,
    ADMIN,
    MEMBER,
}

data class StudyGroupProfileImageDto(
    val type: ProfileImageType = ProfileImageType.PRESET,
    val url: String = ""
)

data class StudyGroupImageDto(
    val type: ProfileImageType = ProfileImageType.PRESET,
    val original: String = "",
    val thumbnail: String = ""
)

data class StudyGroupRecentPostDto(
    val postId: String,
    val title: String,
    val createdAt: LocalDateTime
)

data class StudyGroupRecentScheduleDto(
    val scheduleId: ObjectId,
    val title: String,
    val session: Long,
    val scheduleAt: LocalDateTime
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
    val createdAt: LocalDateTime? = null
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
    val createdAt: LocalDateTime? = null,
    val isDeleted: Boolean = false
)
