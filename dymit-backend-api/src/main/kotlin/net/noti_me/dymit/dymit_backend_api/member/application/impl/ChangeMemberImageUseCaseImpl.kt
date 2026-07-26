package net.noti_me.dymit.dymit_backend_api.member.application.impl

import net.noti_me.dymit.dymit_backend_api.member.application.usecases.ChangeMemberImageUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateMemberProfileImageCommand
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotImplementedException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberPresetImage
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.MemberProfileFilePort
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.dto.MemberProfileFileUploadCommand
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.dto.MemberProfileFileUploadDto
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ChangeMemberImageUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val memberProfileFilePort: MemberProfileFilePort
) : ChangeMemberImageUseCase {

    override fun changeProfileImage(
        loginMember: MemberInfo,
        command: UpdateMemberProfileImageCommand
    ): MemberDto {
        if ( loginMember.memberId != command.memberId ) {
            throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")
        }

        var member = loadMemberPort.loadById(command.memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        val imageVo = when ( command.type ) {
            MemberProfileImageType.EXTERNAL -> {
                if ( command.imageFile == null ) {
                    throw BadRequestException(message = "외부 이미지 업로드를 위한 이미지 파일이 필요합니다.")
                }
                processExternalImageUpload(loginMember, command.imageFile)
            }
            MemberProfileImageType.PRESET -> {
                if ( command.preset == null ) {
                    throw BadRequestException(message = "프리셋 이미지 이름이 필요합니다.")
                }
                processPresetImageSelection(command.preset!!)
            }
            else -> {
                throw NotImplementedException(message = "지원하지 않는 프로필 이미지 타입입니다.")
            }
        }

        member.changeProfileImage(imageVo)
        member = saveMemberPort.update(member)
        return MemberDto.fromEntity(member)
    }


    private fun processExternalImageUpload(memberInfo: MemberInfo, imageFile: MultipartFile): MemberProfileImageVo {
        val result: MemberProfileFileUploadDto = memberProfileFilePort.upload(
            MemberProfileFileUploadCommand(
                memberId = memberInfo.memberId,
                nickname = memberInfo.nickname,
                roles = memberInfo.roles,
                imageFile = imageFile
            )
        )

        return MemberProfileImageVo(
            type = MemberProfileImageType.EXTERNAL,
            fileSize = imageFile.size,
            thumbnail = result.accessUrl,
            original = result.accessUrl,
        )
    }

    private fun processPresetImageSelection(presetImage: MemberPresetImage ): MemberProfileImageVo {
        return MemberProfileImageVo(
            type = MemberProfileImageType.PRESET,
            thumbnail = presetImage.thumbnail,
            original = presetImage.original,
            fileSize = 0L,
            width = 0,
            height = 0,
        )
    }
}
