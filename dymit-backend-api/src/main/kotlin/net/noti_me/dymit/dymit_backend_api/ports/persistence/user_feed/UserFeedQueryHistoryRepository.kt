package net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeedQueryHistory
import org.bson.types.ObjectId

interface UserFeedQueryHistoryRepository {

    fun save(entity: UserFeedQueryHistory): UserFeedQueryHistory

    fun findByMemberId(memberId: ObjectId): UserFeedQueryHistory?
}