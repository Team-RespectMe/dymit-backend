package net.noti_me.dymit.dymit_backend_api.common.errors

class UnauthorizedException(
    code: String = "UNAUTHORIZED",
    override val message: String? = null
) : BusinessException(status=401, code = code, message = message) {
}