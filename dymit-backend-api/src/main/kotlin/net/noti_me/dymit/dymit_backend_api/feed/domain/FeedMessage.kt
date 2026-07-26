package net.noti_me.dymit.dymit_backend_api.feed.domain

/**
 * 피드의 한 메시지 조각입니다.
 *
 * @param text 메시지 본문
 * @param textColor 글자 색상
 * @param highlightColor 강조 색상
 */
data class FeedMessage(
    val text: String,
    val textColor: String? = null,
    val highlightColor: String? = null
)
