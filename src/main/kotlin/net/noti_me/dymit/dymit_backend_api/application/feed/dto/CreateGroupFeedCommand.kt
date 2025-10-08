package net.noti_me.dymit.dymit_backend_api.application.feed.dto

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed

class CreateGroupFeedCommand(
    val groupFeed: GroupFeed
) {

}