package net.noti_me.dymit.dymit_backend_api.application.file.impl

import net.noti_me.dymit.dymit_backend_api.application.file.FileUrlResolver
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.file.dto.UpdateFileStatusCommand
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UpdateFileStatusUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * 파일 상태 갱신 유즈케이스 구현체입니다.
 *
 * @param userFileRepository 파일 메타데이터 저장소
 * @param fileUrlResolver 파일 URL 생성기
 */
@Service
class UpdateFileStatusUseCaseImpl @Autowired constructor(
    private val userFileRepository: UserFileRepository,
    private val fileUrlResolver: FileUrlResolver
) : UpdateFileStatusUseCase {

    constructor(
        userFileRepository: UserFileRepository,
        cdnConfig: CDNConfig
    ) : this(
        userFileRepository = userFileRepository,
        fileUrlResolver = FileUrlResolver(cdnConfig)
    )

    override fun updateStatus(command: UpdateFileStatusCommand): FileDto {
        val userFile = userFileRepository.findById(command.fileId)
            ?: throw NotFoundException(message = "존재하지 않는 파일입니다.")

        if ( userFile.status != command.status && !isAllowedTransition(userFile.status, command.status) ) {
            throw BadRequestException(message = "허용되지 않는 파일 상태 변경입니다.")
        }

        if ( userFile.status == command.status ) {
            return FileDto.from(
                userFile = userFile,
                url = fileUrlResolver.resolve(userFile.path),
                thumbnailUrl = fileUrlResolver.resolveOrNull(userFile.thumbnailPath)
            )
        }

        userFile.updateStatus(command.status)
        val updatedUserFile = userFileRepository.save(userFile)
        return FileDto.from(
            userFile = updatedUserFile,
            url = fileUrlResolver.resolve(updatedUserFile.path),
            thumbnailUrl = fileUrlResolver.resolveOrNull(updatedUserFile.thumbnailPath)
        )
    }

    private fun isAllowedTransition(current: UserFileStatus, next: UserFileStatus): Boolean {
        return when (current) {
            UserFileStatus.REQUESTED -> next == UserFileStatus.UPLOADED || next == UserFileStatus.FAILED
            UserFileStatus.UPLOADED -> next == UserFileStatus.LINKED || next == UserFileStatus.FAILED
            UserFileStatus.LINKED -> next == UserFileStatus.UPLOADED
            UserFileStatus.FAILED -> false
        }
    }
}
