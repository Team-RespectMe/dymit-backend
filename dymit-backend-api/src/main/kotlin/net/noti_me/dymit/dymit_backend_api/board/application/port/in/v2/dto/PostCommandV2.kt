package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto

import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory

/**
 * 게시글 V2 생성/수정 커맨드입니다.
 */
class PostCommandV2(
    val groupId: String,
    val boardId: String,
    val title: String,
    val content: String,
    val category: PostCategory,
    val scheduleId: String?
)
