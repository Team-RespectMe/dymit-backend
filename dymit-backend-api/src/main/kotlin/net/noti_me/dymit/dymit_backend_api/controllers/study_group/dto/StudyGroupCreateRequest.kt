package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import org.hibernate.validator.constraints.Length

@Schema(description = "스터디 그룹 생성 요청 DTO")
@Sanitize
data class StudyGroupCreateRequest(
    @Schema(description = "스터디 그룹 이름", example = "Dymit Study Group")
    @field: Length(min = 1, max = 22, message = "스터디 그룹 이름은 최소 1자 최대 22자까지 입력할 수 있습니다.")
    @field: NotEmpty(message = "스터디 그룹 이름은 비어있을 수 없습니다.")
    val name: String,
    @Schema(description = "스터디 그룹 설명", example = "A study group for Dymit users to collaborate and learn together.")
    @field: Length(max = 500, message = "스터디 그룹 설명은 최대 500자까지 입력할 수 있습니다.")
    @field: NotEmpty(message = "스터디 그룹 설명은 비어있을 수 없습니다.")
    val description: String
) {

    fun toCommand(): StudyGroupCreateCommand {
        return StudyGroupCreateCommand(
            name = name,
            description = description
        )
    }
}