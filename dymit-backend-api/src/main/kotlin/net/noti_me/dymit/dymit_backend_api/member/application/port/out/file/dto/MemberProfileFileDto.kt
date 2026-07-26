package net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.dto

import org.springframework.web.multipart.MultipartFile

/**
 * 멤버 프로필 파일 업로드 명령입니다.
 *
 * @param memberId 멤버 식별자
 * @param nickname 멤버 닉네임
 * @param roles 멤버 권한 이름 목록
 * @param imageFile 업로드할 이미지
 */
data class MemberProfileFileUploadCommand(
    val memberId: String,
    val nickname: String,
    val roles: List<String>,
    val imageFile: MultipartFile
)

/**
 * 멤버 프로필 파일 업로드 결과입니다.
 *
 * @param path 업로드 경로
 * @param accessUrl 접근 URL
 */
data class MemberProfileFileUploadDto(
    val path: String,
    val accessUrl: String
)
