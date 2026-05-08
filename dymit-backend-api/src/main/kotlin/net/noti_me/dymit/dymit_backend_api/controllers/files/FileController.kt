package net.noti_me.dymit.dymit_backend_api.controllers.files

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.file.FileServiceFacade
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.files.dto.FileUploadRequest
import net.noti_me.dymit.dymit_backend_api.controllers.files.dto.FileUploadResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 파일 업로드 API 구현체입니다.
 *
 * 업로드 요청을 수신하고 파일 서비스 파사드로 위임합니다.
 */
@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val fileServiceFacade: FileServiceFacade
) : FileApi {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun uploadFile(
        @LoginMember loginMember: MemberInfo,
        @ModelAttribute @Valid @Sanitize request: FileUploadRequest
    ): FileUploadResponse {
        return FileUploadResponse.from(
            fileServiceFacade.uploadFile(
                loginMember = loginMember,
                command = request.toCommand()
            )
        )
    }
}
