package net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import org.bson.types.ObjectId

interface GroupFeedRepository {

    fun save(groupFeed: GroupFeed): GroupFeed

    fun findById(id: ObjectId): GroupFeed?

    fun findByGroupIdsOrderByIdDesc(groupIds: List<ObjectId>, cursor: ObjectId?, size: Long): List<GroupFeed>
}