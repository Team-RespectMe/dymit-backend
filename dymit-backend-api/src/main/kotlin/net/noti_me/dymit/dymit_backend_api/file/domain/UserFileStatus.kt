package net.noti_me.dymit.dymit_backend_api.file.domain

/**
 * 업로드 파일 상태를 나타내는 enum 입니다.
 */
enum class UserFileStatus {
    REQUESTED,
    UPLOADED,
    LINKED,
    UNREFERENCED,
    DELETED_IN_S3,
    FAILED
}
