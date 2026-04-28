package net.noti_me.dymit.dymit_backend_api.domain.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "user_feeds")
@CompoundIndex(name = "user_feeds_memberId_createdAt_idx", def = "{'memberId': 1, 'createdAt': -1}")
class UserFeed(
    id: ObjectId? = null,
    val memberId: ObjectId,
    val iconType: IconType,
    val eventName: String,
    val messages: List<FeedMessage>,
    val associates: List<AssociatedResource>,
    isRead: Boolean = false,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
) : BaseAggregateRoot<UserFeed>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    companion object {

        fun create(memberId: ObjectId, groupFeed: GroupFeed): UserFeed {
            assert(!groupFeed.isDeleted) { "삭제된 그룹 피드로부터 피드를 생성할 수 없습니다." }
            return UserFeed(
                memberId = memberId,
                iconType = groupFeed.iconType,
                messages = groupFeed.messages,
                associates = groupFeed.associates,
                eventName = groupFeed.eventName,
                createdAt = groupFeed.createdAt,
                updatedAt = groupFeed.updatedAt
            )
        }

        fun create( member: StudyGroupMember, groupFeed: GroupFeed ): UserFeed {
            assert( !member.isDeleted ) { "삭제된 멤버에게 피드를 생성할 수 없습니다." }
            assert( !groupFeed.isDeleted ) { "삭제된 그룹 피드로부터 피드를 생성할 수 없습니다." }
            return UserFeed(
                memberId = member.id!!,
                iconType = groupFeed.iconType,
                eventName = groupFeed.eventName,
                messages = groupFeed.messages,
                associates = groupFeed.associates,
                createdAt = groupFeed.createdAt,
                updatedAt = groupFeed.updatedAt
            )
        }
    }

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

    override fun equals(other: Any?): Boolean {
        if ( this === other ) return true
        if ( other !is UserFeed ) return false
        if ( id == null || other.id == null ) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

