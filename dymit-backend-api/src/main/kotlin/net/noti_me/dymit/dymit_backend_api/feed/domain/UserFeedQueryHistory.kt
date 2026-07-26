package net.noti_me.dymit.dymit_backend_api.feed.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * 회원이 확인한 마지막 그룹 피드 위치입니다.
 *
 * @param id 조회 이력 식별자
 * @param memberId 회원 식별자
 * @param lastFeedId 마지막으로 확인한 그룹 피드 식별자
 */
@Document("user_feed_query_history")
class UserFeedQueryHistory(
    @Id
    val id: ObjectId? = null,
    @Indexed(unique = true, name = "user_feed_query_history_member_id_idx")
    val memberId: ObjectId,
    var lastFeedId: ObjectId? = null
) {

    /**
     * 더 최근의 그룹 피드 식별자로 조회 위치를 갱신합니다.
     *
     * @param newLastGroupQueryId 새 그룹 피드 식별자
     */
    fun updateLastGroupQueryId(newLastGroupQueryId: ObjectId) {
        if (lastFeedId == null || lastFeedId!! < newLastGroupQueryId) {
            lastFeedId = newLastGroupQueryId
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserFeedQueryHistory) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
