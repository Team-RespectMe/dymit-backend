package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.study_group.domain.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.RecentPostVo
import java.time.Instant

@Schema(
    description = "스터디 그룹 상세 조회 응답 DTO",
)
class StudyGroupQueryDetailResponse(
    @field:Schema(description = "스터디 그룹 ID")
    val id: String,
    @field:Schema(description = "스터디 그룹 이름")
    val name: String,
    @field:Schema(description = "그룹 이미지")
    val image: ProfileImageVo = ProfileImageVo(),
    @field: Schema(description = "스터디 그룹 설명")
    val description: String,
    @field:Schema(description = "스터디 그룹 소유자 정보")
    val owner: GroupMemberPreviewResponse,
    @field:Schema(description = "스터디 그룹 멤버 목록")
    val members: List<GroupMemberPreviewResponse>,
    @field:Schema(description = "스터디 그룹 초대 코드 정보")
    val inviteCode: InviteCodeVo = InviteCodeVo(),
    @field:Schema(description = "공지사항 게시판 ID")
    val noticeBoardId: String,
    @field:Schema(description = "최근 공지사항 정보")
    val recentPost: RecentPostVo? = null,
    @field:Schema(description = "스터디 그룹 개설일")
    val createdAt: Instant,
) : BaseResponse() {

    companion object {

        fun of(group: StudyGroupQueryModelDto, members: List<StudyGroupMemberQueryDto>)
        : StudyGroupQueryDetailResponse {
            return StudyGroupQueryDetailResponse(
                id = group.id,
                name = group.name,
                image = ProfileImageVo.from(group.profileImage),
                description = group.description,
                owner = GroupMemberPreviewResponse.from(group.owner),
                members = members.map { GroupMemberPreviewResponse.from(it) },
                inviteCode = group.inviteCode,
                noticeBoardId = group.noticeBoardId,
                recentPost = group.recentPost?.let { it -> RecentPostVo(
                    postId = it.postId,
                    title = it.title,
                    createdAt = it.createdAt
                )},
                createdAt = group.createdAt
            )
        }
    }
}
