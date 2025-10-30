package net.noti_me.dymit.dymit_backend_api.application.file.dto

data class FileUploadResult(
    val bucket: String,
    val key: String,
    val url: String
) {
}