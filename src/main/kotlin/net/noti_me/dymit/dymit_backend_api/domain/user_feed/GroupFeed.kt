package net.noti_me.dymit.dymit_backend_api.domain.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("group_feeds")
class GroupFeed(
    id: ObjectId? = null,
    @Indexed(name = "group_feed_group_id_idx")
    val groupId: ObjectId,
    val iconType: IconType,
    val title: String = "Dymit",
    val messages: List<FeedMessage> = emptyList(),
    val associates: List<AssociatedResource> = emptyList(),
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
): BaseAggregateRoot<GroupFeed> (
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupFeed) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}