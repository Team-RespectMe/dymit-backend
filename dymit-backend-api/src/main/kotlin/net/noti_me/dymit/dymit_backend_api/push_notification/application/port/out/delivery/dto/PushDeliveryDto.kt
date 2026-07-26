package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.dto

/**
 * 외부 푸시 공급자에 전달할 멀티캐스트 메시지입니다.
 */
data class PushDeliveryDto(
    val deviceTokens: List<String>,
    val title: String,
    val body: String,
    val image: String?,
    val data: Map<String, String>
)
