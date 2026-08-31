package net.noti_me.dymit.dymit_backend_api.common.errors

/**
 * 요청 허용 횟수를 초과했을 때 사용하는 비즈니스 예외입니다.
 *
 * @property code 오류 코드
 * @property message 오류 메시지
 */
class TooManyRequestException(
    code: String = "TOO_MANY_REQUESTS",
    override val message: String = "요청 허용 횟수를 초과하였습니다."
) : BusinessException(
    status = 429,
    code = code,
    message = message
)
