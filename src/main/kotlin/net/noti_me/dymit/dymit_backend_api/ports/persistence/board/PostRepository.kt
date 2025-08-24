package net.noti_me.dymit.dymit_backend_api.ports.persistence.board

import net.noti_me.dymit.dymit_backend_api.domain.board.Post

interface PostRepository {

    fun save(post: Post): Post?

    fun findById(id: String): Post?

    fun findByGroupId(groupId: String): List<Post>

    fun findByBoardId(boardId: String): List<Post>

    fun deleteById(id: String): Boolean
}