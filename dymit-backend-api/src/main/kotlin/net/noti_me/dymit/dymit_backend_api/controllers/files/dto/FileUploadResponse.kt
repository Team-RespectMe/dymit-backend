package net.noti_me.dymit.dymit_backend_api.controllers.files.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus

/**
 * 파일 업로드 응답 DTO입니다.
 *
 * @param fileId 파일 문서 ID
 * @param status 파일 상태
 * @param originalFileName 사용자가 업로드한 실제 파일 이름
 * @param path 파일 상대 경로
 * @param url 파일 접근 URL
 * @param thumbnail 파일 썸네일 정보
 */
data class FileUploadResponse(
    @Schema(description = "파일 ID")
    val fileId: String,
    @Schema(description = "파일 상태")
    val status: UserFileStatus,
    @Schema(description = "사용자가 업로드한 실제 파일 이름")
    val originalFileName: String,
    @Schema(description = "파일 상대 경로", example = "/dymit/A/B/UUID_2026_04_27_21_30_10.png")
    val path: String,
    @Schema(description = "파일 접근 URL", example = "https://cdn.example.com/dymit/A/B/UUID_2026_04_27_21_30_10.png")
    val url: String,
    @Schema(description = "이미지 파일인 경우 제공되는 썸네일 정보")
    val thumbnail: FileThumbnailResponse? = null
) : BaseResponse() {

    companion object {

        /**
         * FileDto를 FileUploadResponse로 변환합니다.
         *
         * @param dto 변환할 파일 DTO
         * @return 변환된 응답 DTO
         */
        fun from(dto: FileDto): FileUploadResponse {
            return FileUploadResponse(
                fileId = dto.fileId,
                status = dto.status,
                originalFileName = dto.originalFileName,
                path = dto.path,
                url = dto.url,
                thumbnail = dto.thumbnail?.let(FileThumbnailResponse::from)
            )
        }
    }
}
