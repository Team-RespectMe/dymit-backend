package net.noti_me.dymit.dymit_backend_api.common.errors

class BadRequestException(
    override val message: String? = null,
) : BusinessException(
    status = 400,
    message = message,
) {
}
