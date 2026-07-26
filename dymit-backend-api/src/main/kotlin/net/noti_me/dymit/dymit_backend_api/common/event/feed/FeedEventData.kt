package net.noti_me.dymit.dymit_backend_api.common.event.feed

/**
 * 피드 이벤트에서 사용할 아이콘 종류입니다.
 */
enum class FeedEventIconType {
    APPLAUSE,
    BAD,
    DATE,
    IMPORTANT,
    CHECK,
    NOTICE,
    ROLE
}

/**
 * 피드 이벤트와 연결할 리소스 종류입니다.
 */
enum class FeedEventResourceType {
    MEMBER,
    STUDY_GROUP,
    TASK,
    STUDY_GROUP_OWNER,
    STUDY_GROUP_MEMBER,
    STUDY_GROUP_SCHEDULE,
    STUDY_GROUP_SCHEDULE_COMMENT,
    STUDY_GROUP_BOARD,
    STUDY_GROUP_POST,
    STUDY_GROUP_POST_COMMENT
}

/**
 * 모듈 사이에서 전달하는 피드 메시지 데이터입니다.
 *
 * @param text 메시지 본문
 * @param textColor 글자 색상
 * @param highlightColor 강조 색상
 */
data class FeedEventMessage(
    val text: String,
    val textColor: String? = null,
    val highlightColor: String? = null
)

/**
 * 모듈 사이에서 전달하는 피드 연관 리소스 데이터입니다.
 *
 * @param type 리소스 종류
 * @param resourceId 리소스 식별자
 */
data class FeedEventResource(
    val type: FeedEventResourceType,
    val resourceId: String
)
