package net.noti_me.dymit.dymit_backend_api.application.file.dto

/**
 * 파일 업로드 결과를 나타내는 DTO입니다.
 *
 * @param path 업로드된 파일의 상대 경로
 * @param accessUrl 업로드된 파일의 접근 URL
 */
data class FileUploadResult(
    val path: String,
    val accessUrl: String
)
