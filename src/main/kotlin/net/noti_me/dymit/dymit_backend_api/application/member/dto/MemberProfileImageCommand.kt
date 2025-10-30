package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import org.springframework.web.multipart.MultipartFile

class MemberProfileImageCommand(
    val type: ProfileImageType,
    val presetNo: Int? = null,
    val imageFile: MultipartFile?
) {

}