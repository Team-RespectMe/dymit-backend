package net.noti_me.dymit.dymit_backend_api.domain.board

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("study_group_posts")
class Post(
    @Id
    val id: ObjectId = ObjectId(),
    val groupId: ObjectId,
    val boardId: ObjectId,
    writer: Writer,
    title: String,
    content: String,
    commentCount: Long = 0,
) : BaseAggregateRoot<Post>() {

    var writer: Writer = writer
        private set

    var title = title
        private set

    var content = content
        private set

    var commentCount = commentCount
        private set

    fun setupCommentCount(newCount: Long) {
        if (newCount < 0) {
            throw IllegalArgumentException("Comment count cannot be negative")
        }
        this.commentCount = newCount
    }

    fun updateTitle(requesterId: String, newTitle: String) {
        if (requesterId != writer.id.toHexString()) {
            throw ForbiddenException(message="본인의 게시글이 아닙니다.")
        }

        if (newTitle.isBlank()) {
            throw BadRequestException(message="제목은 비워둘 수 없습니다.")
        }

        this.title = newTitle
    }

    fun updateContent(requesterId: String, newContents: String) {
        if (requesterId != writer.id.toHexString()) {
            throw ForbiddenException(message="본인의 게시글이 아닙니다.")
        }

        if (newContents.isBlank()) {
            throw BadRequestException(message="본문은 비워둘 수 없습니다.")
        }

        this.content = newContents
    }

    fun updateWriterInfo(requesterId: String, newWriter: Writer) {
        if (requesterId != writer.id.toHexString()) {
            throw ForbiddenException(message="본인의 게시글이 아닙니다.")
        }

        if (newWriter.id != this.writer.id) {
            throw IllegalArgumentException("Cannot change writer to a different user")
        }
        this.writer = newWriter
    }
}
