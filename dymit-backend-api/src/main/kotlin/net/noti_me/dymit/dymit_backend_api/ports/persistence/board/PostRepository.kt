package net.noti_me.dymit.dymit_backend_api.ports.persistence.board

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import org.bson.types.ObjectId

interface PostRepository {

    fun save(post: Post): Post

    fun saveAll(posts: List<Post>): List<Post>

    fun findById(id: String): Post?

    fun findByGroupId(groupId: String): List<Post>

    fun findByBoardId(boardId: String): List<Post>

    fun findByBoardIdLteId(boardId: String, lastId: String?, limit: Int): List<Post>

    fun findLastPostByGroupIdAndBoardId(groupId: ObjectId, boardId: ObjectId): Post?

    /**
     * 작성자 ID로 게시글을 조회합니다.
     * lastId가 null이 아니면, lastId 이후의 게시글을 조회합니다.
     * limit는 조회할 게시글의 최대 개수입니다.
     * 게시글은 생성일시(createdAt) 기준 내림차순으로 정렬됩니다.
     * @return 게시글 목록
     */
    fun findByWriterId(writerId: String, lastId: String?, limit: Int): List<Post>

    fun deleteById(id: String): Boolean

    // batch update
    fun updateWriterInfo(member: Member): Int
}