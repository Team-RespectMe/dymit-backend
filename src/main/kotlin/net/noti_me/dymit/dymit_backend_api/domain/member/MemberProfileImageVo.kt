package net.noti_me.dymit.dymit_backend_api.domain.member

data class MemberProfileImageVo(
    val filePath: String = "",
    val cdnUrl: String = "",
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
) {

    fun isValid(): Boolean {
        return filePath.isNotEmpty() && cdnUrl.isNotEmpty() && fileSize > 0 && width > 0 && height > 0
    }
}