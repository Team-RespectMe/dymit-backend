package net.noti_me.dymit.dymit_backend_api.application.file.dto

import org.springframework.web.multipart.MultipartFile

/**
 * 파일 업로드 요청을 서비스 레이어로 전달하는 커맨드입니다.
 *
 * @param file 업로드할 멀티파트 파일
 */
data class FileUploadCommand(
    val file: MultipartFile
) {

    var enforceFileApiPolicy: Boolean = false
        private set

    /**
     * File API 정책 검증을 강제하는 커맨드로 표시합니다.
     *
     * @return FileUploadCommand
     */
    fun enforceFileApiPolicy(): FileUploadCommand {
        this.enforceFileApiPolicy = true
        return this
    }
}
