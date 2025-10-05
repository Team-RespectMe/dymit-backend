package net.noti_me.dymit.dymit_backend_api.domain.user_feed

interface FeedableEvent {

    fun toUserFeed(): UserFeed
}