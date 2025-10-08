package net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

interface UserFeedRepository {

    fun saveAll(userFeeds: List<UserFeed>)

    fun save(userFeed: UserFeed): UserFeed

    fun findById(id: String): UserFeed?

    fun findByMemberIdOrderByCreatedAtDesc(memberId: String, cursor: String?, size: Long): List<UserFeed>

    fun deleteById(id: String): Boolean
}
