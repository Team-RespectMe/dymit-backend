package net.noti_me.dymit.dymit_backend_api.common.response

abstract class BaseResponse (
    val _links: MutableMap<String, HateoasLink> = mutableMapOf()
)
