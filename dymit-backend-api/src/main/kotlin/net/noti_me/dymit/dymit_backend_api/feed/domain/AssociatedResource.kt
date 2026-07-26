package net.noti_me.dymit.dymit_backend_api.feed.domain

/**
 * 피드와 연결된 리소스입니다.
 *
 * @param type 리소스 종류
 * @param resourceId 리소스 식별자
 */
data class AssociatedResource(
    val type: ResourceType,
    val resourceId: String
)
