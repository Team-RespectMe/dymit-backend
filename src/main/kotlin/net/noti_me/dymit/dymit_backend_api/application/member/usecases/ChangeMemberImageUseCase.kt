package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import org.springframework.web.multipart.MultipartFile

interface ChangeMemberImageUseCase {

    fun changeProfileImage(
        loginMember: MemberInfo,
        memberId: String,
        type: ProfileImageType,
        presetNo: Int? = null,
        imageFile: MultipartFile?
    ): MemberDto
}