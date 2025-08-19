package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupMemberPreview
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.RecentScheduleVo

@Schema(
    description = "스터디 그룹 상세 조회 응답 DTO",
)
class StudyGroupQueryDetailResponse(
    val id: String,
    val name: String,
    val description: String,
    val owner: StudyGroupMemberResponse,
    val members: List<GroupMemberPreviewResponse>,
    val inviteCode: String? = null,
    val recentPost: RecentPostVo? = null,
    val createdAt: String,
) : BaseResponse() {

}