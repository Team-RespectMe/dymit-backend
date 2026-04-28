package net.noti_me.dymit.dymit_backend_api.common.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "HATEOAS link")
data class HateoasLink(
    @field: Schema(description = "링크 URL")
    val href: String?
)
