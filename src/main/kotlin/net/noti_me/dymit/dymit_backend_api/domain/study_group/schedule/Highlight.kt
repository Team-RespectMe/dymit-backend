package net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "색상 하이라이트 정보", example = "{\"r\":255,\"g\":0,\"b\":0,\"a\":255}")
class Highlight(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int = 255
) {

}