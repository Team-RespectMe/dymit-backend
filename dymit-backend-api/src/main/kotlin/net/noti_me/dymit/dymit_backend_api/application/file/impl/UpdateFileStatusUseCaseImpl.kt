package net.noti_me.dymit.dymit_backend_api.application.file.impl

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.file.dto.UpdateFileStatusCommand
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UpdateFileStatusUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import org.springframework.stereotype.Service

/**
 * 파일 상태 갱신 유즈케이스 구현체입니다.
 *
 * @param userFileRepository 파일 메타데이터 저장소
 * @param cdnConfig CDN 설정 정보
 */
@Service
class UpdateFileStatusUseCaseImpl(
    private val userFileRepository: UserFileRepository,
    private val cdnConfig: CDNConfig
) : UpdateFileStatusUseCase {

    override fun updateStatus(command: UpdateFileStatusCommand): FileDto {
        val userFile = userFileRepository.findById(command.fileId)
            ?: throw NotFoundException(message = "존재하지 않는 파일입니다.")

        if ( userFile.status != command.status && !isAllowedTransition(userFile.status, command.status) ) {
            throw BadRequestException(message = "허용되지 않는 파일 상태 변경입니다.")
        }

        if ( userFile.status == command.status ) {
            return FileDto.from(
                userFile = userFile,
                url = buildAccessUrl(userFile.path),
                thumbnailUrl = buildOptionalAccessUrl(userFile.thumbnailPath)
            )
        }

        userFile.updateStatus(command.status)
        val updatedUserFile = userFileRepository.save(userFile)
        return FileDto.from(
            userFile = updatedUserFile,
            url = buildAccessUrl(updatedUserFile.path),
            thumbnailUrl = buildOptionalAccessUrl(updatedUserFile.thumbnailPath)
        )
    }

    private fun isAllowedTransition(current: UserFileStatus, next: UserFileStatus): Boolean {
        return when (current) {
            UserFileStatus.REQUESTED -> next == UserFileStatus.UPLOADED || next == UserFileStatus.FAILED
            UserFileStatus.UPLOADED -> next == UserFileStatus.LINKED || next == UserFileStatus.FAILED
            UserFileStatus.LINKED -> false
            UserFileStatus.FAILED -> false
        }
    }

    private fun buildAccessUrl(path: String): String {
        return cdnConfig.getDomain().trimEnd('/') + path
    }

    private fun buildOptionalAccessUrl(path: String?): String? {
        return path?.let(::buildAccessUrl)
    }
}
