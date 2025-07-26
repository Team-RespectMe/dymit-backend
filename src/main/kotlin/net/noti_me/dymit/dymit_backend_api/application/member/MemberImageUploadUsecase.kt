package net.noti_me.dymit.dymit_backend_api.application.member

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.web.multipart.MultipartFile

interface MemberImageUploadUsecase {

    fun uploadImage(loginMember: MemberInfo,
                    memberId: String,
                    type: String,
                    presetNo: Int? = null,
                    imageFile: MultipartFile?
    ): MemberDto
}