package net.noti_me.dymit.dymit_backend_api.application.file.impl

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadResult
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UploadFileUseCase
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UploadProfileImageUseCase
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

    override fun upload(member: MemberInfo, imageFile: MultipartFile): FileUploadResult {
        val result = uploadFileUseCase.uploadFile(
            loginMember = member,
            command = FileUploadCommand(file = imageFile)
        )

        return FileUploadResult(
            path = result.path,
            accessUrl = result.url
        )
    }
}
