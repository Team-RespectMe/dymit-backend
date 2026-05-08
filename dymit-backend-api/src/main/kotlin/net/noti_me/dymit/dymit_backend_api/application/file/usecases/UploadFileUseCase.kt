package net.noti_me.dymit.dymit_backend_api.application.file.usecases

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 파일 업로드 유즈케이스입니다.
 */
interface UploadFileUseCase {

    /**
     * 파일을 업로드하고 메타데이터를 저장합니다.
     *
     * @param loginMember 로그인 사용자 정보
     * @param command 파일 업로드 커맨드
     * @return 업로드 결과 DTO
     */
    fun uploadFile(loginMember: MemberInfo, command: FileUploadCommand): FileDto
}
