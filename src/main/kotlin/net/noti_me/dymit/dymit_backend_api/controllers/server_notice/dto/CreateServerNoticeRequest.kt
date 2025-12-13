package net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.CreateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize

@Schema(description = "서버 공지 생성 요청")
@Sanitize
data class CreateServerNoticeRequest(
    @Schema(description = "공지 제목, 100자 제한", example = "서버 점검 안내")
    @field: Size(min = 1, max = 100, message = "공지 제목은 100자 이내로 작성해야 합니다.")
    val title: String,
    @Schema(description = "공지 내용", example = "안녕하세요, 서버 점검이 예정되어 있습니다...")
    @field: Size(min=1, max = 5000, message = "공지 내용은 5000자 이내로 작성해야 합니다.")
    val content: String,
    @Schema(description = "푸시 알림 발송 여부", example = "false", defaultValue = "false")
    val pushRequired: Boolean = false
) {

    fun toCommand(): CreateServerNoticeCommand {
        return CreateServerNoticeCommand(
            title = this.title,
            content = this.content,
            pushRequired = this.pushRequired
        )
    }
}