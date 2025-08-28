package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.Highlight

/**
 * 스터디 그룹 일정의 역할 할당 정보
 * @param memberId 역할을 할당받은 멤버의 ID
 * @param roleName 할당된 역할 이름
 */
@Schema(description = "스터디 그룹 일정의 역할 할당 정보")
class RoleAssignment(
    @Schema(description = "역할을 할당받은 멤버의 ID", example = "64b8f0c2e1b0c8a1d2f3e4b5")
    val memberId: String,
    @Schema(description = "할당된 역할 이름", example = "[\"자료조사\", \"초기 디자인\"]")
    val roles: List<String>,
    @Schema(description = "하이라이트 색상", example = "{\"r\":255,\"g\":0,\"b\":0,\"a\":255}")
    val color: Highlight
) {

}