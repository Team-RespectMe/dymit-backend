package net.noti_me.dymit.dymit_backend_api.common.errors

class InternalServerError(override val message: String? = null) : BusinessException(500, message) {

}