package net.noti_me.dymit.dymit_backend_api.common.errors

class NotImplementedException(
    override val message: String? = "아직 구현이 되지 않은 기능입니다."
) : BusinessException(
    status = 501,
    code = "not_implemented",
    message = message
) {

}