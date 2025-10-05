package net.noti_me.dymit.dymit_backend_api.application.push_notification.dto


data class PushMessage(
    val deviceToken: String,
    val title: String,
    val body: String,
    val image: String?,
    val data: Map<String, String>
) {

}

