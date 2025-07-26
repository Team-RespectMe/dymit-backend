package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import org.springframework.web.multipart.MultipartFile

class MemberProfileImageCommand(
    val type: String,
    val presetNo: Int? = null,
    val imageFile: MultipartFile?
) {

    init {
        if (type != "preset" && type != "external") {
            throw BadRequestException("Invalid profile image type: $type. Allowed values are 'preset' or 'external'.")
        }
    }
}