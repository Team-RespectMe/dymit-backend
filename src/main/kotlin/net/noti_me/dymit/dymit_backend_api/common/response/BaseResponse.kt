package net.noti_me.dymit.dymit_backend_api.common.response

import io.swagger.v3.oas.annotations.media.Schema


abstract class BaseResponse (
    @field: Schema(name = "_links", description = "HATEOAS 링크들", example = "{\n" +
            "  \"self\": {\n" +
            "    \"href\": \"https://api.example.com/resource\"\n" +
            "  }\n" +
            "}")
    val _links: MutableMap<String, HateoasLink> = mutableMapOf()
)
