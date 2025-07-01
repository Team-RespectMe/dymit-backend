package net.noti_me.dymit.dymit_backend_api.common.errors

open class BusinessException(
    val status: Int = 500,
    override val message: String? = null,
) : RuntimeException(message) {

}