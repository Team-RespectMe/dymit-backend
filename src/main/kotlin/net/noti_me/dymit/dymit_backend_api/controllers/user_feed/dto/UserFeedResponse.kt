package net.noti_me.dymit.dymit_backend_api.controllers.user_feed.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.user_feed.dto.UserFeedDto
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import java.time.LocalDateTime

@Schema(description = "사용자 피드 응답")
data class UserFeedResponse(
    @Schema(description = "피드 ID", example = "507f1f77bcf86cd799439011")
    val id: String,

    @Schema(description = "피드 메시지", example = "새로운 댓글이 달렸습니다.")
    val message: String,

    @Schema(description = "연관 리소스 정보")
    val associates: List<AssociatedResourceResponse>,

    @Schema(description = "생성 시간")
    val createdAt: LocalDateTime,

    @Schema(description = "읽음 여부", example = "false")
    val isRead: Boolean
) {
    companion object {
        fun from(dto: UserFeedDto): UserFeedResponse {
            return UserFeedResponse(
                id = dto.id,
                message = dto.message,
                associates = dto.associates.map { it ->
                    AssociatedResourceResponse.from(it)
                },
                createdAt = dto.createdAt,
                isRead = dto.isRead
            )
        }
    }
}

@Schema(description = "연관 리소스 응답")
data class AssociatedResourceResponse(
    @Schema(description = "리소스 타입", example = "STUDY_GROUP")
    val type: ResourceType,

    @Schema(description = "리소스 ID", example = "507f1f77bcf86cd799439012")
    val resourceId: String
) {
    companion object {
        fun from(associated: AssociatedResource): AssociatedResourceResponse {
            return AssociatedResourceResponse(
                type = associated.type,
                resourceId = associated.resourceId
            )
        }
    }
}
