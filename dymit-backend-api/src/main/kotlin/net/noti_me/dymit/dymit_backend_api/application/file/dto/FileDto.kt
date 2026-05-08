package net.noti_me.dymit.dymit_backend_api.application.file.dto

import net.noti_me.dymit.dymit_backend_api.domain.file.UserFile
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus

/**
 * 업로드 파일 정보를 외부로 전달하는 DTO입니다.
 *
 * @param fileId 파일 문서 ID
 * @param status 파일 상태
 * @param originalFileName 사용자가 업로드한 실제 파일 이름
 * @param path 상대 경로
 * @param url 접근 URL
 * @param thumbnail 썸네일 메타데이터
 */
data class FileDto(
    val fileId: String,
    val status: UserFileStatus,
    val originalFileName: String,
    val path: String,
    val url: String,
    val thumbnail: FileThumbnailDto? = null
) {

    companion object {

        /**
         * UserFile 엔티티를 FileDto로 변환합니다.
         *
         * @param userFile 변환할 파일 엔티티
         * @param url 사용자에게 노출할 접근 URL
         * @param thumbnailUrl 사용자에게 노출할 썸네일 접근 URL
         * @return 변환된 FileDto
         */
        fun from(
            userFile: UserFile,
            url: String,
            thumbnailUrl: String? = null
        ): FileDto {
            return FileDto(
                fileId = userFile.identifier,
                status = userFile.status,
                originalFileName = userFile.originalFileName,
                path = userFile.path,
                url = url,
                thumbnail = if ( userFile.thumbnailPath != null && thumbnailUrl != null ) {
                    FileThumbnailDto(
                        path = userFile.thumbnailPath!!,
                        url = thumbnailUrl
                    )
                } else {
                    null
                }
            )
        }
    }
}
