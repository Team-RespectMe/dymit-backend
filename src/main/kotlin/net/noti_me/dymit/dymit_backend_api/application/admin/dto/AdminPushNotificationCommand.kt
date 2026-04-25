package net.noti_me.dymit.dymit_backend_api.application.admin.dto

data class AdminPushNotificationCommand(
    val message: String,
    val memberIds: List<String>
) {

}
