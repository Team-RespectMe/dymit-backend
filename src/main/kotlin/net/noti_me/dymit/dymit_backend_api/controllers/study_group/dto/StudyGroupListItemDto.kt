package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.SchedulePreview
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupQueryModelDto

@Schema(
    description = "스터디 그룹 목록 조회 응답 DTO",
)
class StudyGroupListItemDto(
    @Schema(description = "스터디 그룹 ID")
    val groupId: String,
    @Schema(description = "스터디 그룹 소유자 정보")
    val owner: MemberPreview,
    @Schema(description = "스터디 그룹 이름")
    val name: String,
    @Schema(description = "스터디 그룹 설명")
    val description: String,
    @Schema(description = "스터디 그룹 멤버 목록")
    val members: List<MemberPreview>,
    @Schema(description = "스터디 그룹 스케줄 정보")
    val schedule: SchedulePreview?,
    @Schema(description = "스터디 그룹 개설 일시")
    val createdAt: String,
) {

    companion object {

        fun from(dto: StudyGroupQueryModelDto): StudyGroupListItemDto {
            return StudyGroupListItemDto(
                groupId = dto.id,
                owner = dto.owner,
                name = dto.name,
                description = dto.description,
                members = dto.members,
                schedule = dto.schedule,
                createdAt = dto.createdAt.toString()
            )
        }
    }
}