package net.noti_me.dymit.dymit_backend_api.application.board.v2

import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.UpdateBoardCategoryPoliciesCommandV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.CreatePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.GetBoardCategoriesUseCaseV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.GetBoardPostsUseCaseV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.GetPostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.RemovePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.UpdateBoardCategoriesUseCaseV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.usecases.UpdatePostUseCaseV2
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import org.springframework.stereotype.Service

/**
 * Board V2 서비스 파사드입니다.
 */
@Service
class BoardServiceFacadeV2(
    private val createPostUseCaseV2: CreatePostUseCaseV2,
    private val updatePostUseCaseV2: UpdatePostUseCaseV2,
    private val removePostUseCaseV2: RemovePostUseCaseV2,
    private val getBoardPostsUseCaseV2: GetBoardPostsUseCaseV2,
    private val getPostUseCaseV2: GetPostUseCaseV2,
    private val getBoardCategoriesUseCaseV2: GetBoardCategoriesUseCaseV2,
    private val updateBoardCategoriesUseCaseV2: UpdateBoardCategoriesUseCaseV2
) {

    fun createPost(memberInfo: MemberInfo, command: PostCommandV2) =
        createPostUseCaseV2.create(memberInfo, command)

    fun updatePost(memberInfo: MemberInfo, postId: String, command: PostCommandV2) =
        updatePostUseCaseV2.update(memberInfo, postId, command)

    fun removePost(memberInfo: MemberInfo, groupId: String, boardId: String, postId: String) =
        removePostUseCaseV2.remove(memberInfo, groupId, boardId, postId)

    fun getBoardPosts(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        cursor: String?,
        size: Int,
        category: PostCategory?
    ) = getBoardPostsUseCaseV2.getPosts(memberInfo, groupId, boardId, cursor, size, category)

    fun getPost(memberInfo: MemberInfo, groupId: String, boardId: String, postId: String) =
        getPostUseCaseV2.get(memberInfo, groupId, boardId, postId)

    fun getBoardCategories(memberInfo: MemberInfo, groupId: String, boardId: String) =
        getBoardCategoriesUseCaseV2.getCategories(memberInfo, groupId, boardId)

    fun updateBoardCategories(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        command: UpdateBoardCategoryPoliciesCommandV2
    ) = updateBoardCategoriesUseCaseV2.updateCategories(memberInfo, groupId, boardId, command)
}
