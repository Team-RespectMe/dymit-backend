package net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto

/**
 * 파일 애플리케이션 경계를 통과하는 파일 상태입니다.
 */
enum class FileStatusDto {
    REQUESTED,
    UPLOADED,
    LINKED,
    UNREFERENCED,
    DELETED_IN_S3,
    FAILED
}
