package net.noti_me.dymit.dymit_backend_api.controllers.user_feed.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.feed.dto.UserFeedDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.controllers.user_feed.vo.FeedMessageVo
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import java.time.LocalDateTime

@Schema(description = "사용자 피드 응답")
data class UserFeedResponse(
    @Schema(description = "피드 ID", example = "6884dec1beed715fdd4a7639")
    val id: String,
    @Schema(description = "피드 아이콘 타입", example = "HAND_WAVING")
    val iconType: IconType,
    @Schema(description = "피드 메시지", example = "새로운 댓글이 달렸습니다.")
    val messages: List<FeedMessageVo> = listOf(
        FeedMessageVo(text= "새로운 스터디 일정이 등록되었어요! ", highlightColor = "#FF5733"),
        FeedMessageVo(text= "[ "),
        FeedMessageVo(text= "2주차 - 포인터 및 배열(더미 데이터입니다.)", highlightColor = "#33C1FF"),
        FeedMessageVo(text= " ]"),
    ),
    @Schema(description = "관련 리소스 목록, 리디렉션 시 사용")
    val resources: List<AssociatedResourceResponse> ,
    @Schema(description = "읽은 여부", example = "false")
    val isRead: Boolean,
    @Schema(description = "생성 일자", example = "2025-10-05T14:48:00")
    val createdAt: LocalDateTime
): BaseResponse() {

    companion object {
        fun from(dto: UserFeedDto): UserFeedResponse {
            return UserFeedResponse(
                id = dto.id,
                iconType = dto.iconType,
                messages = dto.messages,
                resources = dto.associates.map {
                    AssociatedResourceResponse.from(it)
                },
                isRead = dto.isRead,
                createdAt = dto.createdAt
            )
        }
    }
}
