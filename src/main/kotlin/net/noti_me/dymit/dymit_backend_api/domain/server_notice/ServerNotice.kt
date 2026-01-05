package net.noti_me.dymit.dymit_backend_api.domain.server_notice

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "server_notices")
class ServerNotice(
    id: ObjectId? = null,
    category: String,
    val writer: Writer,
    title: String,
    content: String,
    link: Link? = null,
): BaseAggregateRoot<ServerNotice>(id) {

    var title: String = title
        private set

    var content: String = content
        private set

    var link: Link? = link
        private set

    var category: String = category
        private set

    companion object {
        fun create(
            writer: Member,
            category: String,
            title: String,
            content: String,
            link: Link? = null
        ): ServerNotice {
            require(title.isNotEmpty() && title.length <= 100) { "공지사항 제목은 비어있을 수 없으며, 100자를 초과할 수 없습니다." }
            require(content.isNotEmpty()) { "공지사항 내용은 비어있을 수 없습니다." }
            require(category.isNotEmpty()) { "공지사항 카테고리는 비어있을 수 없습니다." }

            if ( !writer.isAdmin() ) {
                throw ForbiddenException(message = "공지사항을 작성할 권한이 없습니다.")
            }

            return ServerNotice(
                writer = Writer.from(writer),
                title = title,
                content = content,
                category = category,
                link = link
            )
        }
    }

    fun updateCategory(requester: Member, newCategory: String) {
        require(newCategory.isNotEmpty()) { "공지사항 카테고리는 비어있을 수 없습니다." }

        if ( !requester.isAdmin() ) {
            throw ForbiddenException(message = "공지사항 카테고리를 수정할 권한이 없습니다.")
        }

        this.category = newCategory
    }

    fun updateTitle(requester: Member, newTitle: String) {
        require(newTitle.isNotEmpty() && newTitle.length <= 100) { "공지사항 제목은 비어있을 수 없으며, 100자를 초과할 수 없습니다." }

        if ( !requester.isAdmin() ) {
            throw ForbiddenException(message = "공지사항 제목을 수정할 권한이 없습니다.")
        }

        this.title = newTitle
    }

    fun updateContent(requester: Member, newContent: String) {
        require(newContent.isNotEmpty()) { "공지사항 내용은 비어있을 수 없습니다." }

        if ( !requester.isAdmin() ) {
            throw ForbiddenException(message = "공지사항 내용을 수정할 권한이 없습니다.")
        }

        this.content = newContent
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerNotice) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode()?:0
    }

    override fun toString(): String {
        return "ServerNotice(id=${id?.toHexString()}, writer=$writer, title='$title', content='$content')"
    }
}
