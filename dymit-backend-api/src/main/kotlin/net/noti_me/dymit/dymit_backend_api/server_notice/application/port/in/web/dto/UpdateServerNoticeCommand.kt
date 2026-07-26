package net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto

import org.bson.types.ObjectId

/**
 * 서버 공지 수정 요청을 애플리케이션 경계로 전달하는 커맨드입니다.
 *
 * @param noticeId 수정할 공지 식별자
 * @param category 공지 카테고리
 * @param title 공지 제목
 * @param content 공지 내용
 */
data class UpdateServerNoticeCommand(
    val noticeId: ObjectId,
    val category: String,
    val title: String,
    val content: String
) {
}
