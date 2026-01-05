package net.noti_me.dymit.dymit_backend_api.application.server_notice.dto

import org.bson.types.ObjectId

data class UpdateServerNoticeCommand(
    val noticeId: ObjectId,
    val category: String,
    val title: String,
    val content: String
) {
}
