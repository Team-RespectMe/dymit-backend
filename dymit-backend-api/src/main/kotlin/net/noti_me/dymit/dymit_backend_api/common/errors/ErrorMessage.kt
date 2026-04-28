package net.noti_me.dymit.dymit_backend_api.common.errors

enum class ErrorMessage(
    val code: String,
    val message: String
) {

    MEMBER_CAN_NOT_LEAVE_OWNED_GROUP(
        code = "M004",
        message = "스터디 그룹을 소유한 회원은 탈퇴할 수 없습니다. 먼저 소유한 스터디 그룹을 다른 회원에게 양도하거나 삭제해 주세요."
    ),
}