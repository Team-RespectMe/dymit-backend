package net.noti_me.dymit.dymit_backend_api.domain.board

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("post_comments")
class PostComment(
    @Id
    val id: ObjectId = ObjectId(),
    val postId: ObjectId,
    val writer: Writer,
    content: String
) : BaseAggregateRoot<PostComment>() {

    var content: String = content
        private set

    fun updateContent(requester: String, newContent: String) {
//        println("requester: $requester, writer: ${writer.id.toHexString()}")
        if (requester != writer.id.toHexString()) {
            throw ForbiddenException(message="본인의 댓글이 아닙니다.")
        }

        if (newContent.isBlank()) {
            throw BadRequestException(message="댓글 본문은 비워둘 수 없습니다.")
        }

        if ( newContent.length > 500 ) {
            throw BadRequestException(message="댓글 본문은 최대 500자까지 작성할 수 있습니다.")
        }

        this.content = newContent
    }
}