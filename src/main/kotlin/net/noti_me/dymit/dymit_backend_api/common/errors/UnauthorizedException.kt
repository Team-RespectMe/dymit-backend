package net.noti_me.dymit.dymit_backend_api.common.errors

class UnauthorizedException(override val message: String? = null) : BusinessException(401, message) {

}