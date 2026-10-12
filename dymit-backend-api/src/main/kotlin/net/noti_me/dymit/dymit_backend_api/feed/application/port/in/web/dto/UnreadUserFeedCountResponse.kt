package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse

/**
 * 읽지 않은 개인 피드 수 응답입니다.
 *
 * @param unreadCount 읽지 않은 피드 수
 */
@Schema(description = "읽지 않은 개인 피드 수 응답")
@JsonIgnoreProperties("_links")
data class UnreadUserFeedCountResponse(
    @field:Schema(description = "읽지 않은 피드 수", example = "3")
    val unreadCount: Long
) : BaseResponse()
