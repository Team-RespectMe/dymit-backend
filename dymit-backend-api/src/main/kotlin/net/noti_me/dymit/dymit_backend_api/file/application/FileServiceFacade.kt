package net.noti_me.dymit.dymit_backend_api.file.application

import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.UpdateFileStatusCommand
import net.noti_me.dymit.dymit_backend_api.file.application.usecase.UpdateFileStatusUseCase
import net.noti_me.dymit.dymit_backend_api.file.application.usecase.UploadFileUseCase
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
        return uploadFileUseCase.execute(loginMember, command)
    }

    fun updateFileStatus(command: UpdateFileStatusCommand): FileDto {
        return updateFileStatusUseCase.execute(command)
    }
}
