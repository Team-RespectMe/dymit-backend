package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query

import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member.dto.StudyGroupMemberData
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import java.time.LocalDateTime

class StudyGroupQueryModelDto(
    val id: String,
    val name: String,
    val profileImage: GroupProfileImageVo,
    val owner: MemberPreview,
    val description: String,
    val noticeBoardId: String,
    var recentPost: PostPreview? = null,
    var recentSchedule: SchedulePreview? = null,
    val inviteCode: InviteCodeVo = InviteCodeVo(),
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(
            entity: StudyGroup,
            owner: StudyGroupMemberData,
            noticeBoardId: String = ""
        ): StudyGroupQueryModelDto {
            return StudyGroupQueryModelDto(
                id = entity.identifier,
                name = entity.name,
                profileImage = entity.profileImage,
                owner = MemberPreview.of(owner, GroupMemberRole.OWNER),
                noticeBoardId = noticeBoardId,
                description = entity.description,
                recentPost = entity.recentPost?.let { PostPreview.from(it) },
                recentSchedule = entity.recentSchedule?.let { SchedulePreview.from(it) },
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}
