package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.ProfileImageVo
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document

/**
 * 스터디 그룹 일정 역할 할당 정보
 * @param memberId 멤버 ID
 * @param nickname 멤버 닉네임
 * @param image 멤버 프로필 이미지
 * @param roles 멤버가 가진 역할 목록
 */
class ScheduleRole(
    val memberId: ObjectId,
    val nickname: String,
    val image: ProfileImageVo,
    val roles: List<String>
) {

    fun isRoleChanged(newRoles: List<String>): Boolean {
        return roles != newRoles
    }

    override fun equals(other: Any?): Boolean {
        if ( this === other) return true
        if (other !is ScheduleRole) return false

        return memberId == other.memberId
    }

    override fun hashCode(): Int {
        return memberId.hashCode()
    }
}