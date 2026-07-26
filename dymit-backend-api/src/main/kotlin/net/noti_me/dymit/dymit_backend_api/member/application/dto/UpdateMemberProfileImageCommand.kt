package net.noti_me.dymit.dymit_backend_api.member.application.dto

import net.noti_me.dymit.dymit_backend_api.member.domain.MemberProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberPresetImage
import org.springframework.web.multipart.MultipartFile

class UpdateMemberProfileImageCommand(
    val memberId: String,
    val type: MemberProfileImageType,
    val preset: MemberPresetImage? = null,
    val imageFile: MultipartFile? = null
) {

}
