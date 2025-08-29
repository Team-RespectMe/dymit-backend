package net.noti_me.dymit.dymit_backend_api.domain.study_group

/**
 * 스터디 그룹의 프로필 이미지를 나타내는 Value Object
 * filePath : 프로필 이미지 파일의 저장 경로
 * url : 프로필 이미지의 CDN URL
 * fileSize : 프로필 이미지 파일의 크기 (바이트 단위)
 * width : 프로필 이미지의 너비 (픽셀 단위)
 * height : 프로필 이미지의 높이 (픽셀 단위)
 */
data class GroupProfileImageVo (
    val filePath: String = "",
    val type: String = "preset", // 프로필 이미지 타입, 기본값은 'preset'
    val url: String = "",
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
) {

}
