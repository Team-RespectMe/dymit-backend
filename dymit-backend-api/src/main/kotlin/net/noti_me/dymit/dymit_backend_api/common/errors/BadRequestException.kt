package net.noti_me.dymit.dymit_backend_api.common.errors

class BadRequestException(
    code: String = "BAD_REQUEST",
    override val message: String = "요청이 잘못되어 처리할 수 없습니다."
) : BusinessException(
    status = 400,
    code = code,
    message = message,
) {
}
