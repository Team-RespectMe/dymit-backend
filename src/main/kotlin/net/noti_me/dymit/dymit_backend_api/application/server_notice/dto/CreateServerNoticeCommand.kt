package net.noti_me.dymit.dymit_backend_api.application.server_notice.dto

data class CreateServerNoticeCommand(
    val title: String,
    val content: String,
    val pushRequired: Boolean = false
) {
}