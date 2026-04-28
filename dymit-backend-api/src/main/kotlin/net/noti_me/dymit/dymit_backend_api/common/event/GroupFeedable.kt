package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed

interface GroupFeedable {

    fun toGroupFeed(): GroupFeed
}