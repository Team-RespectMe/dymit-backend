package net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.board.domain.PostComment
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.dto.BoardWriterUpdateDto

interface CommentRepository {

    fun save(comment: PostComment): PostComment

    fun saveAll(comments: List<PostComment>): List<PostComment>

    fun findById(id: String): PostComment?

    fun findByWriterId(
        writerId: String,
        lastId: String?,
        limit: Int
    ): List<PostComment>

    fun findByPostId(postId: String): List<PostComment>

    fun findByPostIdLteId(
        postId: String,
        lastId: String?,
        size: Int
    ): List<PostComment>

    fun deleteById(id: String): Boolean

    fun delete(comment: PostComment): Boolean

    fun updateWriterInfo(writer: BoardWriterUpdateDto): Int
}
