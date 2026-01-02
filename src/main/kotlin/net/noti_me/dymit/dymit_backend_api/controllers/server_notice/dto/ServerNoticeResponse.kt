package net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.WriterVo
import net.noti_me.dymit.dymit_backend_api.domain.server_notice.Link
import java.time.LocalDateTime

@Schema(description = "서버 공지 응답 DTO")
data class ServerNoticeResponse(
    @Schema(description = "공지 ID", example = "64b8f0c2e1b2c3d4e5f67890")
    val id: String,
    @Schema(description = "공지 제목", example = "서버 점검 안내")
    val title: String,
    @Schema(description = "공지 내용", example = "안녕하세요, 서버 점검이 예정되어 있습니다...")
    val content: String,
    @Schema(description = "공지 링크, 공지 탭을 클릭하는 경우 해당 페이지로 이동")
    val link: Link? = null,
    @Schema(description = "공지 생성 일시", example = "2024-06-15T14:30:00")
    val createdAt: LocalDateTime,
    @Schema(description = "공지 수정 일시", example = "2024-06-16T10:15:00")
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from( dto: ServerNoticeDto ) = ServerNoticeResponse(
            id = dto.id.toString(),
            title = dto.title,
            content = dto.content,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            link = dto.link
        )
    }
}