package net.noti_me.dymit.dymit_backend_api.domain.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "user_feeds")
class UserFeed(
    @Id
    val id: ObjectId = ObjectId(),
    val memberId: ObjectId,
    val message: String,
    val associates: List<AssociatedResource>,
    isRead: Boolean = false,
) : BaseAggregateRoot<UserFeed>() {

    var isRead: Boolean = isRead
        private set

    fun markAsRead() {
        if (!isRead) {
            isRead = true
        }
    }

    fun isOwnedBy(memberId: String): Boolean {
        return this.memberId.toHexString() == memberId
    }
}

enum class ResourceType {
    MEMBER,
    STUDY_GROUP,
    STUDY_GROUP_MEMBER,
    STUDY_GROUP_SCHEDULE,
    STUDY_GROUP_POST,
    STUDY_GROUP_POST_COMMENT
}

data class AssociatedResource(
    val type: ResourceType,
    val resourceId: String
)
