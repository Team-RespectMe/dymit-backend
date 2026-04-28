package net.noti_me.dymit.dymit_backend_api.application.feed.impl

import net.noti_me.dymit.dymit_backend_api.application.feed.GroupFeedService
import net.noti_me.dymit.dymit_backend_api.application.feed.dto.CreateGroupFeedCommand
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeedQueryHistory
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.GroupFeedRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedQueryHistoryRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class GroupFeedServiceImpl(
    private val groupFeedRepository: GroupFeedRepository,
    private val userFeedQueryHistoryRepository: UserFeedQueryHistoryRepository
): GroupFeedService {

    override fun createGroupFeed(command: CreateGroupFeedCommand): GroupFeed {
        return groupFeedRepository.save(command.groupFeed)
    }

    override fun pullUnreadGroupFeeds(groupIds: List<ObjectId>, cursor: ObjectId?, size: Long): List<GroupFeed> {
        return groupFeedRepository.findByGroupIdsOrderByIdDesc(
            groupIds = groupIds,
            cursor = cursor,
            size = size
        )
    }

    override fun updateLastReadAt(memberId: ObjectId, lastReadId: ObjectId) {
        val history = userFeedQueryHistoryRepository.findByMemberId(memberId)
            ?: userFeedQueryHistoryRepository.save(
                UserFeedQueryHistory(
                    memberId = memberId,
                    lastFeedId = lastReadId
                )
            )
        history.updateLastGroupQueryId(lastReadId)
        userFeedQueryHistoryRepository.save(history)
    }
}