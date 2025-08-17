package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

/**
 * 스터디 그룹 일정의 역할 할당 정보
 * @param memberId 역할을 할당받은 멤버의 ID
 * @param roleName 할당된 역할 이름
 */
class RoleAssignment(
    val memberId: String,
    val roles: List<String>
) {

}