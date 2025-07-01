package net.noti_me.dymit.dymit_backend_api.common.errors

class ForbiddenException(
    override val message: String? = null,
): BusinessException(
    status = 403,
    message = message,
) {
}
