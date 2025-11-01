package net.noti_me.dymit.dymit_backend_api.domain.board.event

import net.noti_me.dymit.dymit_backend_api.common.event.PersonalPushEvent
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup

class PostCommentCreatedEvent(
    val group: StudyGroup,
    val board: Board,
    val post: Post,
    val comment: PostComment
): PersonalPushEvent(comment) {

    override fun processPushMessage(): PersonalPushMessage {
        return PersonalPushMessage(
            memberId = post.writer.id,
            title = "회원님의 게시글에 댓글이 달렸어요!",
            body = comment.content,
            data = mapOf(
                "type" to "POST_COMMENT",
                "groupId" to group.identifier,
                "boardId" to board.identifier,
                "postId" to comment.postId.toHexString(),
                "commentId" to comment.identifier
            ),
            image = group.profileImage.thumbnail
        )
    }
}