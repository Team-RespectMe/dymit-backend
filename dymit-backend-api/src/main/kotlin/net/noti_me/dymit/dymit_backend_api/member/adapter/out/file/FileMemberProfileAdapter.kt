package net.noti_me.dymit.dymit_backend_api.member.adapter.`out`.file

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.file.application.usecase.UploadProfileImageUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.MemberProfileFilePort
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.dto.MemberProfileFileUploadCommand
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.dto.MemberProfileFileUploadDto
import org.springframework.stereotype.Component

/**
 * 멤버 프로필 파일 출력 포트를 File 모듈에 연결합니다.
 *
 * @param uploadProfileImageUseCase 프로필 이미지 업로드 유즈케이스
 */
@Component
class FileMemberProfileAdapter(
    private val uploadProfileImageUseCase: UploadProfileImageUseCase
) : MemberProfileFilePort {

    override fun upload(command: MemberProfileFileUploadCommand): MemberProfileFileUploadDto {
        val result = uploadProfileImageUseCase.execute(
            member = MemberInfo.of(
                memberId = command.memberId,
                nickname = command.nickname,
                roles = command.roles
            ),
            imageFile = command.imageFile
        )
        return MemberProfileFileUploadDto(
            path = result.path,
            accessUrl = result.accessUrl
        )
    }
}
