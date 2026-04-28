package net.noti_me.dymit.dymit_backend_api.domain.user_feed

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("user_feed_query_history")
class UserFeedQueryHistory(
    @Id
    val id: ObjectId? = null,
    @Indexed(unique = true, name = "user_feed_query_history_member_id_idx")
    val memberId: ObjectId,
    var lastFeedId: ObjectId? = null
//    lastReadGroupFeedId: ObjectId? = null,
) {

//        protected set

    fun updateLastGroupQueryId(newLastGroupQueryId: ObjectId) {
        // null 이거나, lastFeedId보다 이전에 생성된 id가 들어오면 무시한다.
        if ( lastFeedId == null ) {
            lastFeedId = newLastGroupQueryId
        } else {
            // 마지막 쿼리 ID보다 큰값이 들어오면 갱신
            if ( lastFeedId!! < newLastGroupQueryId ) {
                lastFeedId = newLastGroupQueryId
            }
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