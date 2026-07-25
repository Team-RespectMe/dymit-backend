package net.noti_me.dymit.dymit_backend_api.member.application.usecases

import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateMemberProfileImageCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import org.springframework.web.multipart.MultipartFile

interface ChangeMemberImageUseCase {

    fun changeProfileImage(
        loginMember: MemberInfo,
        command: UpdateMemberProfileImageCommand
    ): MemberDto
}
