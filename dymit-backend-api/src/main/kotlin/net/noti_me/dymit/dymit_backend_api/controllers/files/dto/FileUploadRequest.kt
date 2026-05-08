package net.noti_me.dymit.dymit_backend_api.controllers.files.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import org.springframework.web.multipart.MultipartFile

/**
 * 파일 업로드 요청 DTO입니다.
 *
 * @param file 업로드할 멀티파트 파일
 */
@Sanitize
class FileUploadRequest(
    @field:NotNull(message = "업로드할 파일은 필수입니다.")
    @field:Schema(description = "업로드할 파일", requiredMode = Schema.RequiredMode.REQUIRED)
    val file: MultipartFile? = null
) {

    /**
     * 파일 업로드 커맨드로 변환합니다.
     *
     * @return FileUploadCommand
     */
    fun toCommand(): FileUploadCommand {
        return FileUploadCommand(
            file = requireNotNull(file) { "업로드할 파일은 필수입니다." }
        ).enforceFileApiPolicy()
    }
}
