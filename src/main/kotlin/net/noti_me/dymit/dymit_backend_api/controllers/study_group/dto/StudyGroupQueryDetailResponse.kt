package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentPostVo
import java.time.LocalDateTime

@Schema(
    description = "스터디 그룹 상세 조회 응답 DTO",
)
class StudyGroupQueryDetailResponse(
    @field:Schema(description = "스터디 그룹 ID")
    val id: String,
    @field:Schema(description = "스터디 그룹 이름")
    val name: String,
    @field: Schema(description = "스터디 그룹 설명")
    val description: String,
    @field:Schema(description = "스터디 그룹 소유자 정보")
    val owner: GroupMemberPreviewResponse,
    @field:Schema(description = "스터디 그룹 멤버 목록")
    val members: List<GroupMemberPreviewResponse>,
    @field:Schema(description = "스터디 그룹 초대 코드 정보")
    val inviteCode: InviteCodeVo = InviteCodeVo(),
    @field:Schema(description = "최근 공지사항 정보")
    val recentPost: RecentPostVo? = null,
    @field:Schema(description = "스터디 그룹 개설일")
    val createdAt: LocalDateTime,
) : BaseResponse() {

    companion object {

        fun of(group: StudyGroupQueryModelDto, members: List<StudyGroupMemberQueryDto>)
        : StudyGroupQueryDetailResponse {
            return StudyGroupQueryDetailResponse(
                id = group.id,
                name = group.name,
                description = group.description,
                owner = GroupMemberPreviewResponse.from(group.owner),
                members = members.map { GroupMemberPreviewResponse.from(it) },
                inviteCode = group.inviteCode,
                recentPost = RecentPostVo(
                    postId = group.recentPost?.postId ?: "",
                    title = group.recentPost?.title ?: "아직 올라온 공지사항이 없습니다.",
                    createdAt = group.recentPost?.createdAt ?: LocalDateTime.now()
                ),
                createdAt = group.createdAt
            )
        }
    }
}