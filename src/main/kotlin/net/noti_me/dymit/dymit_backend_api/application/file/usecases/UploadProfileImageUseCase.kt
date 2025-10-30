package net.noti_me.dymit.dymit_backend_api.application.file.usecases

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadResult
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.web.multipart.MultipartFile

interface UploadProfileImageUseCase {

    fun upload(member: MemberInfo, imageFile: MultipartFile): FileUploadResult 
}

