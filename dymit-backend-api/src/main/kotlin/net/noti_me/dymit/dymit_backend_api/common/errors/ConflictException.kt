package net.noti_me.dymit.dymit_backend_api.common.errors

class ConflictException(code: String = "CONFLICT", override val message: String) : BusinessException(
    status = 409,
    code = code,
    message = message
) {
    constructor() : this(code = "CONFLICT", message = "요청이 충돌하여 처리할 수 없습니다.")
}