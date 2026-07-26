package net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.server_notice.domain.Link
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNotice
import java.time.LocalDateTime

/**
 * 서버 공지 요약 정보를 애플리케이션 경계 밖으로 전달하는 DTO입니다.
 *
 * @param id 공지 식별자
 * @param category 공지 카테고리
 * @param title 공지 제목
 * @param link 공지 연결 정보
 * @param createdAt 생성 시각
 */
class ServerNoticeSummaryDto(
    val id: String,
    val category: String,
    val title: String,
    val link : Link? = null,
    val createdAt: LocalDateTime,
) {

    companion object {

        /**
         * 서버 공지 엔티티를 요약 DTO로 변환합니다.
         *
         * @param entity 변환할 서버 공지
         * @return 변환된 서버 공지 요약 DTO
         */
        fun from(entity: ServerNotice): ServerNoticeSummaryDto {
            return ServerNoticeSummaryDto(
                id = entity.identifier,
                category = entity.category,
                title = entity.title,
                link = entity.link,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}
