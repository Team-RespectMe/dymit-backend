package net.noti_me.dymit.dymit_backend_api.application.file.dto

import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus

/**
 * 파일 상태 갱신 요청 커맨드입니다.
 *
 * @param fileId 상태를 변경할 파일 ID
 * @param status 변경할 목표 상태
 */
data class UpdateFileStatusCommand(
    val fileId: String,
    val status: UserFileStatus
)
