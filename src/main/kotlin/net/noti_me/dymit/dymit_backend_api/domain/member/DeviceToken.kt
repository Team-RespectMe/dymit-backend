
package net.noti_me.dymit.dymit_backend_api.domain.member

import java.time.Instant


data class DeviceToken(
    val token: String,
    var isActive: Boolean = true,
) 