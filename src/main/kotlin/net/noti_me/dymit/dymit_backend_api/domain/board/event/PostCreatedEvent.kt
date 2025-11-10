package net.noti_me.dymit.dymit_backend_api.domain.board.event

import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType

class PostCreatedEvent(
    val group: StudyGroup,
    val board: Board,
    val post: Post
): GroupImportantEvent(post) {

    private val eventName: String = "POST_CREATED"

    override fun processGroupFeed(): GroupFeed {
        return GroupFeed(
            groupId = group.id!!,
            messages = listOf(
                FeedMessage("${group.name}에 새로운 공지사항이 있어요!")
            ),
            eventName = eventName,
            iconType = IconType.NOTICE,
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP,
                    resourceId = group.identifier
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_BOARD,
                    resourceId = board.identifier
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_POST,
                    resourceId = post.identifier
                )
            ),
        )
    }

    override fun processGroupPush(): GroupPushMessage {
        return GroupPushMessage(
            groupId = group.id!!,
            title = "${group.name}의 새로운 공지사항",
            body = "${post.title}",
            image = group.profileImage.thumbnail,
            eventName = eventName,
            data = mapOf(
                "groupId" to group.identifier,
                "boardId" to board.identifier,
                "postId" to post.identifier,
            )
        )
    }
}
