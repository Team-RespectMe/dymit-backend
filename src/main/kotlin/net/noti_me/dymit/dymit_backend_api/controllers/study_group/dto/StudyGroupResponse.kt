package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupSummaryDto
import java.time.LocalDateTime

@Schema(
    description = "스터디 그룹 응답 DTO",
)
class StudyGroupResponse(
    @Schema(description = "스터디 그룹 ID")
    val groupId: String,
    @Schema(description = "스터디 그룹 이름")
    val name: String,
    @Schema(description = "스터디 그룹 설명")
    val description: String,
    @Schema(description = "스터디 그룹 소유자 정보")
    val owner: String,
    @Schema(description = "스터디 그룹 개설 일시")
    val createdAt: LocalDateTime,
    @Schema(description = "스터디 그룹 초대 코드 정보")
    val inviteCodeVo: InviteCodeVo,
) {

    companion object {

        fun from(obj: StudyGroupDto): StudyGroupResponse {
            return StudyGroupResponse(
                groupId = obj.groupId,
                name = obj.name,
                description = obj.description,
                owner = obj.ownerId,
                inviteCodeVo = obj.inviteCodeVo,
                createdAt = obj.createdAt
            )
        }

        fun from(obj: StudyGroupSummaryDto): StudyGroupResponse {
            return StudyGroupResponse(
                groupId = obj.id,
                name = obj.name,
                description = obj.description,
                owner = obj.owner.memberId,
                inviteCodeVo = InviteCodeVo(code="", expireAt = LocalDateTime.MAX, createdAt = LocalDateTime.now()),
                createdAt = obj.createdAt
            )
        }
    }
}