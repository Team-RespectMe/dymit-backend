package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberPresetImage
import org.springframework.web.multipart.MultipartFile

class UpdateMemberProfileImageCommand(
    val memberId: String,
    val type: ProfileImageType,
    val preset: MemberPresetImage? = null,
    val imageFile: MultipartFile? = null
) {

}
