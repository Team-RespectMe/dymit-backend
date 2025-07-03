package net.noti_me.dymit.dymit_backend_api.common.errors

class NotFoundException(
    code: String = "NOT_FOUND",
    override val message : String? = "요청한 리소스를 찾을 수 없습니다."
) : BusinessException(status = 404, message = message) {

    constructor() : this(code = "NOT_FOUND")
}