package net.noti_me.dymit.dymit_backend_api.common.errors

class ForbiddenException(
    code: String = "FORBIDDEN",
    override val message: String? = "권한이 없어 요청을 처리할 수 없습니다."
): BusinessException(
    status = 403,
    message = message,
) {
}
