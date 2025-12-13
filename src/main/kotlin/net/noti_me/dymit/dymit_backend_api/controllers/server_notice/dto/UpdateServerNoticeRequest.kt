package net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.UpdateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import org.bson.types.ObjectId

@Schema(description = "서버 공지 수정 요청")
@Sanitize
data class UpdateServerNoticeRequest(
    @Schema(description = "공지 제목, 100자 제한", example = "서버 점검 안내")
    val title: String,
    @Schema(description = "공지 내용", example = "안녕하세요, 서버 점검이 예정되어 있습니다...")
    val content: String,
) {

    fun toCommand(noticeId: String): UpdateServerNoticeCommand {
        return UpdateServerNoticeCommand(
            noticeId = ObjectId(noticeId),
            title = this.title,
            content = this.content
        )
    }
}