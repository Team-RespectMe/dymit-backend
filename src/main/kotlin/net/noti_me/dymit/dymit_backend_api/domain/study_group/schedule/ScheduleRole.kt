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
    val color: String = "#FF3357",
    val roles: List<String>
) {

    /**
     * 새로운 역할 목록이 기존 역할 목록과 다른지 여부를 반환합니다.
     * @param newRoles 새로운 역할 목록
     * @return 역할 목록이 변경되었으면 true, 그렇지 않으면 false
     */
    fun isRoleChanged(newRoles: ScheduleRole): Boolean {
        if ( this != newRoles ) throw IllegalArgumentException("Cannot compare roles for different members")
        if (roles.size != newRoles.roles.size) return true
        return roles.sorted() != newRoles.roles.sorted()
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