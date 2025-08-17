package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.SchedulePreview
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroup
import java.time.LocalDateTime

class StudyGroupQueryModelDto(
    val id: String,
    val name: String,
    val profileImage: GroupProfileImageVo,
    val owner: MemberPreview,
    val description: String,
    var recentPost: PostPreview? = null,
    var recentSchedule: SchedulePreview? = null,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(entity: StudyGroup, owner: Member): StudyGroupQueryModelDto {
            return StudyGroupQueryModelDto(
                id = entity.id.toHexString(),
                name = entity.name,
                profileImage = entity.profileImage,
                owner = MemberPreview.of(owner, GroupMemberRole.OWNER),
                description = entity.description,
                recentPost = entity.recentPost?.let { PostPreview.from(it) },
                recentSchedule = entity.recentSchedule?.let { SchedulePreview.from(it) },
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}