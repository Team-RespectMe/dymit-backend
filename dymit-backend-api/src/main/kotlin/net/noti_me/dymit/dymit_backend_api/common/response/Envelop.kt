package net.noti_me.dymit.dymit_backend_api.common.response

import org.slf4j.MDC
import java.util.UUID

data class Envelop(
    val traceId: String? = MDC.get("traceId"),
    val status: Int = 200,
    val data: Any? = null
) {

}