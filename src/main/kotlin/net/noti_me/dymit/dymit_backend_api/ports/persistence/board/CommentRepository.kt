package net.noti_me.dymit.dymit_backend_api.ports.persistence.board

import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment

interface CommentRepository {

    fun save(comment: PostComment): PostComment

    fun findById(id: String): PostComment?

    fun findByPostId(postId: String): List<PostComment>

    fun deleteById(id: String): Boolean

    fun delete(comment: PostComment): Boolean
}