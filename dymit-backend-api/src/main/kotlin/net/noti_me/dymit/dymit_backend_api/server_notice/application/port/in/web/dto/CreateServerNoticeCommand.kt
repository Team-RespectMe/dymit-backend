package net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto

/**
 * 서버 공지 생성 요청을 애플리케이션 경계로 전달하는 커맨드입니다.
 *
 * @param category 공지 카테고리
 * @param title 공지 제목
 * @param content 공지 내용
 * @param pushRequired 푸시 알림 필요 여부
 */
data class CreateServerNoticeCommand(
    val category: String,
    val title: String,
    val content: String,
    val pushRequired: Boolean = false
) {
}
