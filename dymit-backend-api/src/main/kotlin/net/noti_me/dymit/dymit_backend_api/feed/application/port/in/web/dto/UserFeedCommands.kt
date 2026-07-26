package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto

/**
 * 개인 피드 목록 조회 명령입니다.
 *
 * @param memberId 요청 회원 식별자
 * @param cursorId 조회 커서
 * @param size 조회 개수
 */
data class GetUserFeedsCommand(
    val memberId: String,
    val cursorId: String?,
    val size: Int
)

/**
 * 개인 피드 삭제 명령입니다.
 *
 * @param memberId 요청 회원 식별자
 * @param feedId 삭제할 피드 식별자
 */
data class DeleteUserFeedCommand(
    val memberId: String,
    val feedId: String
)

/**
 * 개인 피드 읽음 처리 명령입니다.
 *
 * @param memberId 요청 회원 식별자
 * @param feedId 읽음 처리할 피드 식별자
 */
data class MarkUserFeedAsReadCommand(
    val memberId: String,
    val feedId: String
)
