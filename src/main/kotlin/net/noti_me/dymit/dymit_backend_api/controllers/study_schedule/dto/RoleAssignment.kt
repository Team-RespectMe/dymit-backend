package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import org.hibernate.validator.constraints.Length

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
    @Schema(description = "하이라이트 색상, #으로 시작 반드시 7자", example = "#FF5733")
    @field:NotEmpty(message = "색상은 비어 있을 수 없습니다.")
    @field:Length(min = 7, max = 7, message = "색상은 반드시 7자여야 합니다.")
    val color: String = "#FF3357",
) {

}