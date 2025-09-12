package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupModifyCommand
import org.hibernate.validator.constraints.Length

@Schema(
    description = "스터디 그룹 수정 요청",
)
class StudyGroupModifyRequest(
    @field: Schema(
        description = "스터디 그룹 이름",
        example = "Dymit 스터디 그룹",
    )
    @field: NotEmpty(message = "스터디 그룹 이름은 비어있을 수 없습니다.")
    @field: Length(min = 1, max = 22, message = "스터디 그룹 이름은 최대 22자까지 입력할 수 있습니다.")
    val name: String,
    @field: Schema(
        description = "스터디 그룹 설명",
        example = "Dymit 사용자들이 함께 협력하고 학습할 수 있는 스터디 그룹입니다.",
    )
    @field: NotEmpty(message = "스터디 그룹 설명은 비어있을 수 없습니다.")
    @field: Length(max = 500, message = "스터디 그룹 설명은 최대 500자까지 입력할 수 있습니다.")
    val description: String
) {

    fun toCommand(groupId: String): StudyGroupModifyCommand {
        return StudyGroupModifyCommand(
            groupId = groupId,
            name = name,
            description = description
        )
    }
}