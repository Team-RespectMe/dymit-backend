package net.noti_me.dymit.dymit_backend_api.file.application

import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.FileUploadResult
import net.noti_me.dymit.dymit_backend_api.file.application.usecase.UploadFileUseCase
import net.noti_me.dymit.dymit_backend_api.file.application.usecase.UploadProfileImageUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * 프로필 이미지 업로드 유즈케이스 구현체입니다.
 *
 * @param uploadFileUseCase 공용 파일 업로드 유즈케이스
 */
@Service
class UploadProfileImageUseCaseImpl(
    private val uploadFileUseCase: UploadFileUseCase
) : UploadProfileImageUseCase {

    override fun execute(member: MemberInfo, imageFile: MultipartFile): FileUploadResult {
        val result = uploadFileUseCase.execute(
            loginMember = member,
            command = FileUploadCommand(file = imageFile)
        )

        return FileUploadResult(
            path = result.path,
            accessUrl = result.url
        )
    }
}
