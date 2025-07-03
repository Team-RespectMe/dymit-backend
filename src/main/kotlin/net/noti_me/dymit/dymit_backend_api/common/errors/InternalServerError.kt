package net.noti_me.dymit.dymit_backend_api.common.errors

class InternalServerError(override val message: String? = null) : BusinessException(status = 500, code = "INTERNAL_SERVER_ERROR", message=message) {

}