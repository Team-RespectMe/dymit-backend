package net.noti_me.dymit.dymit_backend_api.controllers.user_feed.vo

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage

@Schema(description = "피드 메시지 파트")
data class FeedMessageVo(
    @Schema(description = "피드 메시지", example = "새로운 댓글이 달렸습니다.")
    val text: String,
    @Schema(description = "피드 메시지 컬러(nullable)", example = "#FF5733")
    val textColor: String? = null,
    @Schema(description = "피드 메시지 하이라이트 컬러(nullable)", example = "#33FF57")
    val highlightColor: String? = null
) {

    companion object {
        fun from(message: FeedMessage): FeedMessageVo {
            return FeedMessageVo(
                text = message.text,
                textColor = message.textColor,
                highlightColor = message.highlightColor
            )
        }
    }
}