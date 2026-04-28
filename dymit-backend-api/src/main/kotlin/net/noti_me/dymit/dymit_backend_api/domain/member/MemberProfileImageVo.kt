package net.noti_me.dymit.dymit_backend_api.domain.member

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType

/**
 * 멤버 프로필 이미지 VO
 * @param type 프로필 이미지 타입 (presets: 기본 이미지, upload: 업로드된 이미지)
 * @param thumbnail 프로필 이미지 썸네일 경로
 * @param original 프로필 이미지 원본 경로
 * @param fileSize 프로필 이미지 파일 크기
 * @param width 프로필 이미지 너비
 * @param height 프로필 이미지 높이
 */
class MemberProfileImageVo(
    val type: ProfileImageType = ProfileImageType.PRESET,
    val thumbnail: String = MemberPresetImage.CHECK.thumbnail,
    val original: String = MemberPresetImage.CHECK.original,
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemberProfileImageVo) return false

        if (type != other.type) return false
        if (thumbnail != other.thumbnail) return false
        if (original != other.original) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + thumbnail.hashCode()
        result = 31 * result + original.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}
