package net.noti_me.dymit.dymit_backend_api.server_notice.domain

import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNoticeProfileImageType
import org.bson.types.ObjectId

/**
 * 서버 공지 변경을 요청하는 멤버의 최소 값입니다.
 *
 * @param id 멤버 식별자
 * @param nickname 멤버 닉네임
 * @param imageType 프로필 이미지 유형
 * @param imageUrl 프로필 썸네일 URL
 * @param admin 관리자 여부
 */
data class ServerNoticeRequester(
    val id: ObjectId,
    val nickname: String,
    val imageType: ServerNoticeProfileImageType,
    val imageUrl: String,
    val admin: Boolean
)
