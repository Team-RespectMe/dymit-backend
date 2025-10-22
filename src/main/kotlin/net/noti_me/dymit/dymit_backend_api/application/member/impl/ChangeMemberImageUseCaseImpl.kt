package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.usecases.ChangeMemberImageUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotImplementedException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ChangeMemberImageUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort
) : ChangeMemberImageUseCase {

    override fun changeProfileImage(
        loginMember: MemberInfo,
        memberId: String,
        type: ProfileImageType,
        presetNo: Int?,
        imageFile: MultipartFile?
    ): MemberDto {
        if ( loginMember.memberId != memberId ) {
            throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")
        }

        var member = loadMemberPort.loadById(memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        if ( imageFile != null ) {
            throw NotImplementedException(message = "이미지 업로드 기능은 아직 구현되지 않았습니다.")
        } else {
            if ( presetNo == null ) {
                throw BadRequestException(message = "이미지 업로드를 위한 이미지 파일 또는 프리셋 번호가 필요합니다.")
            }

            member.changeProfileImage(getPresetImage(presetNo))
        }

        member = saveMemberPort.update(member)
        return MemberDto.fromEntity(member)
    }

    private fun getExternalImage(
        loginMember: MemberInfo,
        memberId: String,
        type: ProfileImageType,
        imageFile: MultipartFile
    ): MemberProfileImageVo {
        return MemberProfileImageVo(
            filePath = "external/${loginMember.memberId}/$memberId/$type/${imageFile.originalFilename}",
            fileSize = imageFile.size,
            url = "https://example.com/external/${loginMember.memberId}/$memberId/$type/${imageFile.originalFilename}",
            type = type,
            width = 0, // TODO: 이미지 크기 계산 로직 필요
            height = 0 // TODO: 이미지 크기 계산 로직 필요
        )
    }

    private fun getPresetImage(presetNo: Int): MemberProfileImageVo {
        return MemberProfileImageVo(
            filePath = "",
            fileSize = 0L,
            url = "${presetNo}",
            type = ProfileImageType.PRESET,
            width = 0,
            height = 0,
        )
    }

    private fun processExternalImageUpload() {

    }

    private fun processPresetImageSelection() {

    }
}