package net.noti_me.dymit.dymit_backend_api.domain.studyGroup

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id

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

    /**
     * 프로필 이미지가 유효한지 검사하는 메서드
     * 유효성 검사 기준:
     * - fileId, fileName, url은 비어있지 않아야 함
     * - fileSize는 0보다 커야 함
     * - width와 height는 0보다 커야 함
     */
    fun isValid() : Boolean {
        return filePath.isNotEmpty() && url.isNotEmpty() && fileSize > 0 && width > 0 && height > 0
    }
}
