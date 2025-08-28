package net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule

import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import org.bson.types.ObjectId

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
    val color: Highlight = Highlight(255, 0, 0, 255),
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