package net.noti_me.dymit.dymit_backend_api.domain.member

import com.fasterxml.jackson.annotation.JsonIgnore

class MemberProfileImageVo(
    val filePath: String = "",
    val url: String = "",
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
) {

    @JsonIgnore
    fun isValid(): Boolean {
        return filePath.isNotEmpty() && url.isNotEmpty() && fileSize > 0 && width > 0 && height > 0
    }
}