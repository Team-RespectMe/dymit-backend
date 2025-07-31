package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupJoinCommand
import org.hibernate.validator.constraints.Length

@Schema(description = "스터디 그룹 가입 요청 DTO")
class StudyGroupJoinRequest(
    @Schema(description = "스터디 그룹 초대 코드", example = "A0B1C2D3")
    @field: Length(min=8, max=8, message = "초대 코드는 8자리여야 합니다.")
    @field: NotEmpty(message = "초대 코드는 비어있을 수 없습니다.")
    val inviteCode: String,
) {

    fun toCommand(groupId: String): StudyGroupJoinCommand {
        return StudyGroupJoinCommand(
            inviteCode = inviteCode,
            groupId = groupId
        )
    }
}