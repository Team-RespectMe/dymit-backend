package net.noti_me.dymit.dymit_backend_api.application.board

import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostCommand
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface PostService {

    fun createPost(
        memberInfo: MemberInfo,
        command: PostCommand
    ): PostDto

    fun updatePost(
        memberInfo: MemberInfo,
        postId: String,
        command: PostCommand
    ): PostDto

    fun removePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    )

    fun getBoardPosts(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String
    ): List<PostDto>

    fun getPost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ): PostDto
}