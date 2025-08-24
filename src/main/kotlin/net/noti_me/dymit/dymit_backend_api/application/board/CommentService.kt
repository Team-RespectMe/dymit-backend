package net.noti_me.dymit.dymit_backend_api.application.board

import net.noti_me.dymit.dymit_backend_api.application.board.dto.CommentCommand
import net.noti_me.dymit.dymit_backend_api.application.board.dto.CommentDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface CommentService {

    fun createComment(
        memberInfo: MemberInfo,
        command: CommentCommand
    ): CommentDto

    fun updateComment(
        memberInfo: MemberInfo,
        commentId: String,
        command: CommentCommand
    ): CommentDto

    fun removeComment(
        memberInfo: MemberInfo,
        commentId: String
    )

    fun getPostComments(
        memberInfo: MemberInfo,
        postId: String
    ): List<CommentDto>
}