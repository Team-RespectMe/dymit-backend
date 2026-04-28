package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEvent

abstract class GroupImportantEvent(source: Any)
: ApplicationEvent(source), GroupPushable, GroupFeedable {

    private val excludedMemberIds: MutableSet<ObjectId> = mutableSetOf()

    protected abstract fun processGroupFeed(): GroupFeed

    protected abstract fun processGroupPush(): GroupPushMessage

    final override fun toGroupFeed(): GroupFeed {
        val feed = processGroupFeed()
        if ( this.excludedMemberIds.isNotEmpty() ) {
            feed.excludedMemberIds.addAll(this.excludedMemberIds)
        }
        return feed
    }

    final override fun toGroupPush(): GroupPushMessage {
        if ( this.excludedMemberIds.isNotEmpty() ) {
            val push = processGroupPush()
            push.excluded.addAll(this.excludedMemberIds)
            return push
        }
        return processGroupPush()
    }

    fun addExcludedMemberId(memberId: ObjectId) {
        excludedMemberIds.add(memberId)
    }

    fun filterExcludedMemberIds(memberIds: MutableSet<ObjectId>): MutableSet<ObjectId> {
        memberIds.removeAll(excludedMemberIds)
        return memberIds
    }
}