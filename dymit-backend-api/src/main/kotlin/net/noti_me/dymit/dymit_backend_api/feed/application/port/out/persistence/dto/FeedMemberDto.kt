package net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto

import java.time.LocalDateTime

/**
 * Feed 모듈에서 사용하는 회원 조회 결과입니다.
 *
 * @param createdAt 회원 가입 시각
 */
data class FeedMemberDto(
    val createdAt: LocalDateTime
)
