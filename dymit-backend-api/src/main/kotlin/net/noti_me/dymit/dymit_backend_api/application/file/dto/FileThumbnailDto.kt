package net.noti_me.dymit.dymit_backend_api.application.file.dto

/**
 * 파일 썸네일 정보를 외부로 전달하는 DTO입니다.
 *
 * @param path 썸네일 상대 경로
 * @param url 썸네일 접근 URL
 */
data class FileThumbnailDto(
    val path: String,
    val url: String
)
