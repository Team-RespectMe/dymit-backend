package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto

import net.noti_me.dymit.dymit_backend_api.feed.domain.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.feed.domain.FeedMessage
import net.noti_me.dymit.dymit_backend_api.feed.domain.IconType

/**
 * 개인 피드 생성 명령입니다.
 *
 * @param memberId 수신 회원 식별자
 * @param iconType 아이콘 종류
 * @param eventName 이벤트 이름
 * @param messages 메시지 목록
 * @param associates 연결 리소스 목록
 */
data class CreatePersonalFeedCommand(
    val memberId: String,
    val iconType: IconType,
    val eventName: String,
    val messages: List<FeedMessage>,
    val associates: List<AssociatedResource>
)

/**
 * 그룹 피드 생성 명령입니다.
 *
 * @param groupId 대상 그룹 식별자
 * @param iconType 아이콘 종류
 * @param eventName 이벤트 이름
 * @param title 피드 제목
 * @param messages 메시지 목록
 * @param associates 연결 리소스 목록
 * @param excludedMemberIds 제외 회원 식별자
 */
data class CreateGroupFeedCommand(
    val groupId: String,
    val iconType: IconType,
    val eventName: String,
    val title: String = "Dymit",
    val messages: List<FeedMessage>,
    val associates: List<AssociatedResource>,
    val excludedMemberIds: Set<String> = emptySet()
)
