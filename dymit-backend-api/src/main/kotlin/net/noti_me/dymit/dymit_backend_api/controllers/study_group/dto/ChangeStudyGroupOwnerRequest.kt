package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.ChangeGroupOwnerCommand

@Schema(
    description = "스터디 그룹 소유자 변경 요청",
)
data class ChangeStudyGroupOwnerRequest(
    @field: NotEmpty(message = "새로운 소유자 ID는 비어있을 수 없습니다.")
    @field: Schema(description = "새로운 소유자 ID", example = "60b8d295f1d2c926d8f97b10")
    val newOwnerId: String
) {

    fun toCommand(groupId: String): ChangeGroupOwnerCommand {
        return ChangeGroupOwnerCommand(
            groupId = groupId,
            newOwnerId = newOwnerId
        )
    }
}