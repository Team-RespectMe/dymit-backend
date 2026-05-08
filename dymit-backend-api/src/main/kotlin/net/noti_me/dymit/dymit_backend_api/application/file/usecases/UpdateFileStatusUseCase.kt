package net.noti_me.dymit.dymit_backend_api.application.file.usecases

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.file.dto.UpdateFileStatusCommand

/**
 * 파일 상태 갱신 유즈케이스입니다.
 */
interface UpdateFileStatusUseCase {

    /**
     * 파일 상태를 갱신합니다.
     *
     * @param command 상태 변경 커맨드
     * @return 변경된 파일 DTO
     */
    fun updateStatus(command: UpdateFileStatusCommand): FileDto
}
