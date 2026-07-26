package net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNoticeRequester
import org.bson.types.ObjectId

/**
 * 서버 공지 모듈이 사용하는 멤버 조회 DTO입니다.
 *
 * @param id 멤버 식별자
 * @param nickname 멤버 닉네임
 * @param imageType 프로필 이미지 유형
 * @param imageUrl 프로필 썸네일 URL
 * @param admin 관리자 여부
 */
data class ServerNoticeMemberDto(
    val id: ObjectId,
    val nickname: String,
    val imageType: ProfileImageType,
    val imageUrl: String,
    val admin: Boolean
) {

    /**
     * 서버 공지 도메인의 요청자 값으로 변환합니다.
     *
     * @return 서버 공지 요청자
     */
    fun toRequester(): ServerNoticeRequester {
        return ServerNoticeRequester(
            id = id,
            nickname = nickname,
            imageType = imageType,
            imageUrl = imageUrl,
            admin = admin
        )
    }
}
