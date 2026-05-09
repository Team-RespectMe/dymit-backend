package net.noti_me.dymit.dymit_backend_api.controllers.files.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileThumbnailDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse

/**
 * 파일 썸네일 응답 DTO입니다.
 *
 * @param path 썸네일 상대 경로
 * @param url 썸네일 접근 URL
 */
class FileThumbnailResponse(
    @Schema(description = "썸네일 상대 경로", example = "/dymit/thumbnails/A/B/UUID_2026_04_27_21_30_10_thumbnail.jpg")
    val path: String,
    @Schema(description = "썸네일 접근 URL", example = "https://cdn.example.com/dymit/thumbnails/A/B/UUID_2026_04_27_21_30_10_thumbnail.jpg")
    val url: String
): BaseResponse() {

    companion object {

        /**
         * FileThumbnailDto를 FileThumbnailResponse로 변환합니다.
         *
         * @param dto 변환할 썸네일 DTO
         * @return 변환된 썸네일 응답 DTO
         */
        fun from(dto: FileThumbnailDto): FileThumbnailResponse {
            return FileThumbnailResponse(
                path = dto.path,
                url = dto.url
            )
        }
    }
}
