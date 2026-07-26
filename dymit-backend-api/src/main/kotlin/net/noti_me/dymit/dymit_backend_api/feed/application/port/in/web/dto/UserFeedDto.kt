package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.feed.domain.IconType
import net.noti_me.dymit.dymit_backend_api.feed.domain.ResourceType
import java.time.LocalDateTime

/**
 * 웹 입력 포트가 반환하는 개인 피드 데이터입니다.
 *
 * @param id 피드 식별자
 * @param memberId 회원 식별자
 * @param iconType 아이콘 종류
 * @param eventName 이벤트 이름
 * @param messages 표시할 메시지 목록
 * @param associates 연결 리소스 목록
 * @param createdAt 생성 시각
 * @param isRead 읽음 여부
 */
data class UserFeedDto(
    val id: String,
    val memberId: String,
    val iconType: IconType,
    val eventName: String = "",
    val messages: List<FeedMessageDto>,
    val associates: List<AssociatedResourceDto>,
    val createdAt: LocalDateTime,
    val isRead: Boolean
)

/**
 * 웹 입력 포트가 반환하는 피드 메시지입니다.
 *
 * @param text 메시지 본문
 * @param textColor 글자 색상
 * @param highlightColor 강조 색상
 */
data class FeedMessageDto(
    val text: String,
    val textColor: String? = null,
    val highlightColor: String? = null
)

/**
 * 웹 입력 포트가 반환하는 연관 리소스입니다.
 *
 * @param type 리소스 종류
 * @param resourceId 리소스 식별자
 */
data class AssociatedResourceDto(
    val type: ResourceType,
    val resourceId: String
)
