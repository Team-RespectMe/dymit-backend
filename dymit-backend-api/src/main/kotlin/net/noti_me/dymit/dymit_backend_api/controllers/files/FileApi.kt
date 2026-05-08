package net.noti_me.dymit.dymit_backend_api.controllers.files

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.files.dto.FileUploadRequest
import net.noti_me.dymit.dymit_backend_api.controllers.files.dto.FileUploadResponse

/**
 * 파일 업로드 API 인터페이스입니다.
 */
@Tag(name = "파일 API", description = "파일 업로드 관련 API")
interface FileApi {

    /**
     * 파일을 업로드합니다.
     *
     * @param loginMember 로그인 사용자 정보
     * @param request 업로드 요청 DTO
     * @return 업로드 결과 응답
     */
    @Operation(method = "POST", summary = "파일 업로드", description = "멀티파트 파일을 S3에 업로드하고 메타데이터를 저장합니다.")
    @ApiResponse(responseCode = "201", description = "파일 업로드 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun uploadFile(
        loginMember: MemberInfo,
        @Valid request: FileUploadRequest
    ): FileUploadResponse
}
