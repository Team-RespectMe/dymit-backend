package net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2

import net.noti_me.dymit.dymit_backend_api.board.domain.Post
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory
import org.bson.types.ObjectId

/**
 * 게시글 V2 영속성 포트입니다.
 */
interface PostRepositoryV2 {

    fun save(post: Post): Post

    fun findById(id: String): Post?

    fun findByBoardIdLteId(
        boardId: String,
        lastId: String?,
        limit: Int,
        category: PostCategory?
    ): List<Post>

    fun findLastPostByGroupIdAndBoardId(groupId: ObjectId, boardId: ObjectId): Post?

    fun deleteById(id: String): Boolean
}
