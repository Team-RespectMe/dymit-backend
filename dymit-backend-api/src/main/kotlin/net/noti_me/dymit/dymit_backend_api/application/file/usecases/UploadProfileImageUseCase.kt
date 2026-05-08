package net.noti_me.dymit.dymit_backend_api.application.file.usecases

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadResult
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.web.multipart.MultipartFile

/**
 * 프로필 이미지 업로드 유즈케이스입니다.
 */
interface UploadProfileImageUseCase {

    /**
     * 프로필 이미지를 업로드합니다.
     *
     * @param member 로그인 사용자 정보
     * @param imageFile 업로드할 이미지 파일
     * @return 업로드 결과
     */
    fun upload(member: MemberInfo, imageFile: MultipartFile): FileUploadResult
}
