package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.common.event.GroupFeedable
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEvent

abstract class GroupFeedEvent(source: Any)
: ApplicationEvent(source), GroupFeedable {

    private val excludedMemberIds: MutableSet<ObjectId> = mutableSetOf()

    abstract fun processGroupFeed(): GroupFeed

    final override fun toGroupFeed(): GroupFeed {
        val feed = processGroupFeed()

        if ( this.excludedMemberIds.isNotEmpty() ) {
            feed.excludedMemberIds.addAll(this.excludedMemberIds)
        }

        return feed
    }

    fun addExcludedMemberId(memberId: ObjectId) {
        excludedMemberIds.add(memberId)
    }

    fun filterExcludedMemberIds(memberIds: MutableSet<ObjectId>): MutableSet<ObjectId> {
        memberIds.removeAll(excludedMemberIds)
        return memberIds
    }
}