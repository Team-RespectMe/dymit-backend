package net.noti_me.dymit.dymit_backend_api.common.event.feed

/**
 * 그룹 피드 생성에 필요한 공통 이벤트 계약입니다.
 */
interface GroupFeedEvent {

    /**
     * 그룹 피드 데이터를 반환합니다.
     *
     * @return 그룹 피드 데이터
     */
    fun toGroupFeedData(): GroupFeedEventData
}

/**
 * 그룹 피드 이벤트가 전달하는 모듈 독립 데이터입니다.
 *
 * @param groupId 대상 그룹 식별자
 * @param iconType 아이콘 종류
 * @param eventName 이벤트 이름
 * @param title 피드 제목
 * @param messages 표시할 메시지 목록
 * @param resources 연결 리소스 목록
 * @param excludedMemberIds 피드 생성 제외 회원 식별자
 */
data class GroupFeedEventData(
    val groupId: String,
    val iconType: FeedEventIconType,
    val eventName: String,
    val title: String = "Dymit",
    val messages: List<FeedEventMessage>,
    val resources: List<FeedEventResource>,
    val excludedMemberIds: Set<String> = emptySet()
)
