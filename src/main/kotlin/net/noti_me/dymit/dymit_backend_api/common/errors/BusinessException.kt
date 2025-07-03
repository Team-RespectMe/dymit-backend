package net.noti_me.dymit.dymit_backend_api.common.errors

open class BusinessException(
    val status: Int = 500,
    val code: String = "INTERNAL_SERVER_ERROR",
    override val message: String? = null,
) : RuntimeException(message) {

}