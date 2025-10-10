package net.noti_me.dymit.dymit_backend_api.domain.member

/**
 * 멤버 프로필 이미지 VO
 * @param type 프로필 이미지 타입 (presets: 기본 이미지, upload: 업로드된 이미지)
 * @param filePath 프로필 이미지 파일 경로
 * @param url 프로필 이미지 URL
 * @param fileSize 프로필 이미지 파일 크기
 * @param width 프로필 이미지 너비
 * @param height 프로필 이미지 높이
 */
class MemberProfileImageVo(
    val type: String = "presets",
    val filePath: String = "",
    val url: String = "0",
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
) {

}