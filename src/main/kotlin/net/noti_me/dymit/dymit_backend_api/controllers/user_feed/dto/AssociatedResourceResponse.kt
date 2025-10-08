package net.noti_me.dymit.dymit_backend_api.controllers.user_feed.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType

@Schema(description = "연관 리소스 응답 객체")
data class AssociatedResourceResponse(
    @Schema(description = "리소스 타입")
    val type: ResourceType,
    @Schema(description = "리소스 ID", example = "6884dec1beed715fdd4a7639")
    val resourceId: String
) {

    companion object {
        fun from(resource: AssociatedResource): AssociatedResourceResponse {
            return AssociatedResourceResponse(
                type = resource.type,
                resourceId = resource.resourceId
            )
        }
    }
}