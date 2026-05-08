package net.noti_me.dymit.dymit_backend_api.application.file

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.application.file.dto.UpdateFileStatusCommand
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UpdateFileStatusUseCase
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UploadFileUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 파일 서비스 파사드입니다.
 */
@Service
class FileServiceFacade(
    private val uploadFileUseCase: UploadFileUseCase,
    private val updateFileStatusUseCase: UpdateFileStatusUseCase
) {

    fun uploadFile(loginMember: MemberInfo, command: FileUploadCommand): FileDto {
        return uploadFileUseCase.uploadFile(loginMember, command)
    }

    fun updateFileStatus(command: UpdateFileStatusCommand): FileDto {
        return updateFileStatusUseCase.updateStatus(command)
    }
}
