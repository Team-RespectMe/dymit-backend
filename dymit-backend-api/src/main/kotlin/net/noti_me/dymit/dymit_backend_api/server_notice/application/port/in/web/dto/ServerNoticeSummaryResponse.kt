package net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.Link
import java.time.Instant

@Schema(description = "서버 공지 요약 응답 DTO")
data class ServerNoticeSummaryResponse(
    @Schema(description = "공지 ID", example = "64b8f0c2e1b2c3d4e5f67890")
    val id: String,
    @Schema(description = "공지 카테고리", example = "이벤트")
    val category: String,
    @Schema(description = "공지 제목", example = "서버 점검 안내")
    val title: String,
    @Schema(description = "공지 링크, 공지 탭을 클릭하는 경우 해당 페이지로 이동, nullable", example = "{\"url\": \"https://example.com/notice-details\"}")
    val link: Link ? = null,
    @Schema(description = "공지 생성 일시", example = "2024-06-15T14:30:00")
    val createdAt: Instant
) {

    companion object {
        fun from(dto: ServerNoticeSummaryDto) = ServerNoticeSummaryResponse(
            id = dto.id,
            category = dto.category,
            title = dto.title,
            link = dto.link,
            createdAt = dto.createdAt
        )
    }
}
