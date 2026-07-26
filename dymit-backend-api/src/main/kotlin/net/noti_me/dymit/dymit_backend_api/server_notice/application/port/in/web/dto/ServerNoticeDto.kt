package net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.server_notice.domain.Link
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNotice
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNoticeWriter
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * 서버 공지 상세 정보를 애플리케이션 경계 밖으로 전달하는 DTO입니다.
 *
 * @param id 공지 식별자
 * @param category 공지 카테고리
 * @param writer 공지 작성자
 * @param title 공지 제목
 * @param content 공지 내용
 * @param link 공지 연결 정보
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 */
class ServerNoticeDto(
    val id: ObjectId,
    val category: String,
    val writer: ServerNoticeWriter,
    val title: String,
    val content: String,
    val link: Link?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {

        /**
         * 서버 공지 엔티티를 상세 DTO로 변환합니다.
         *
         * @param notice 변환할 서버 공지
         * @return 변환된 서버 공지 상세 DTO
         */
        fun from(notice: ServerNotice): ServerNoticeDto {
//            println("notice : $notice")
            return ServerNoticeDto(
                id = notice.id!!,
                category = notice.category,
                writer = notice.writer,
                title = notice.title,
                content = notice.content,
                createdAt = notice.createdAt!!,
                updatedAt = notice.updatedAt!!,
                link = notice.link
            )
        }
    }
}
