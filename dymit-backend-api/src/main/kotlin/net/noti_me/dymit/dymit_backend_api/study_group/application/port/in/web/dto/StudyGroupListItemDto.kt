package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.SchedulePreview
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType
import java.time.LocalDateTime

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
    val image: ProfileImageResponse = ProfileImageResponse(
        type = StudyGroupProfileImageType.PRESET,
        url = "0",
    ),
    @Schema(description = "스터디 그룹 설명")
    val description: String,
    @Schema(description = "스터디 그룹 스케줄 중 가장 가까운 미래 스케줄")
    val schedule: SchedulePreview?,
    @Schema(description = "스터디 그룹 개설 일시")
    val createdAt: LocalDateTime,
): BaseResponse() {

    companion object {

        fun from(dto: StudyGroupQueryModelDto): StudyGroupListItemDto {
            return StudyGroupListItemDto(
                groupId = dto.id,
                owner = dto.owner,
                name = dto.name,
                image = ProfileImageResponse.of(
                    type = dto.profileImage.type,
                    url = dto.profileImage.thumbnail
                ),
                description = dto.description,
                schedule = dto.recentSchedule,
                createdAt = dto.createdAt
            )
        }
    }
}
