package net.noti_me.dymit.dymit_backend_api.common.event.feed

/**
 * 개인 피드 생성에 필요한 공통 이벤트 계약입니다.
 */
interface PersonalFeedEvent {

    /**
     * 수신자별 개인 피드 데이터를 반환합니다.
     *
     * @return 개인 피드 데이터 목록
     */
    fun toPersonalFeedData(): List<PersonalFeedEventData>
}

/**
 * 개인 피드 이벤트가 전달하는 모듈 독립 데이터입니다.
 *
 * @param memberId 수신 회원 식별자
 * @param iconType 아이콘 종류
 * @param eventName 이벤트 이름
 * @param messages 표시할 메시지 목록
 * @param resources 연결 리소스 목록
 */
data class PersonalFeedEventData(
    val memberId: String,
    val iconType: FeedEventIconType,
    val eventName: String,
    val messages: List<FeedEventMessage>,
    val resources: List<FeedEventResource>
)
