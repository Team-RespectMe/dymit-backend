package net.noti_me.dymit.dymit_backend_api.board.application.event

import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResource
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.GroupFeedEventData
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.board.domain.Board
import net.noti_me.dymit.dymit_backend_api.board.domain.Post
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.GroupPushMessage

class PostCreatedEvent(
    val group: BoardStudyGroupDto,
    val board: Board,
    val post: Post
): GroupImportantEvent(post) {

    private val eventName: String = "POST_CREATED"

    override fun processGroupFeedData(): GroupFeedEventData {
        return GroupFeedEventData(
            groupId = group.identifier,
            messages = listOf(
                FeedEventMessage("${group.name}에 새로운 공지사항이 있어요!")
            ),
            eventName = eventName,
            iconType = FeedEventIconType.NOTICE,
            resources = listOf(
                FeedEventResource(
                    type = FeedEventResourceType.STUDY_GROUP,
                    resourceId = group.identifier
                ),
                FeedEventResource(
                    type = FeedEventResourceType.STUDY_GROUP_BOARD,
                    resourceId = board.identifier
                ),
                FeedEventResource(
                    type = FeedEventResourceType.STUDY_GROUP_POST,
                    resourceId = post.identifier
                ),
                FeedEventResource(
                    type = FeedEventResourceType.STUDY_GROUP_OWNER,
                    resourceId = group.ownerId.toHexString()
                )
            )
        )
    }

    override fun processGroupPush(): GroupPushMessage {
        return GroupPushMessage(
            groupId = group.id,
            title = "${group.name}의 새로운 공지사항",
            body = "${post.title}",
            image = group.profileImageThumbnail,
            eventName = eventName,
            data = mapOf(
                "groupId" to group.identifier,
                "boardId" to board.identifier,
                "postId" to post.identifier,
                "ownerId" to group.ownerId.toHexString()
            )
        )
    }
}
