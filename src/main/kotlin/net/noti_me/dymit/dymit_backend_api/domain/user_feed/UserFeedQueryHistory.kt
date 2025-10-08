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
        if (lastFeedId == null || newLastGroupQueryId > lastFeedId!!) {
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