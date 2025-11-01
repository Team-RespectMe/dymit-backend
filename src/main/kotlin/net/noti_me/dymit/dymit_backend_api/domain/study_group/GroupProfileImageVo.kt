package net.noti_me.dymit.dymit_backend_api.domain.study_group

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType

/**
 * 스터디 그룹의 프로필 이미지를 나타내는 Value Object
 * original : 프로필 이미지의 원본 파일 경로
 * thumbnail : 프로필 이미지의 썸네일 파일 경로
 * fileSize : 프로필 이미지 파일의 크기 (바이트 단위)
 * width : 프로필 이미지의 너비 (픽셀 단위)
 * height : 프로필 이미지의 높이 (픽셀 단위)
 */
data class GroupProfileImageVo (
    val type: ProfileImageType = ProfileImageType.PRESET,// 프로필 이미지 타입, 기본값은 'preset'
    val original: String = GroupPresetImage.STUDY.original,
    val thumbnail: String = GroupPresetImage.STUDY.thumbnail,
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
) {

}
