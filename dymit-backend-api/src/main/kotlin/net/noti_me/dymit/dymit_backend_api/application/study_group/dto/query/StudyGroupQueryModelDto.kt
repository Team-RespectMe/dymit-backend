package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
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
        fun from(entity: StudyGroup, owner: Member): StudyGroupQueryModelDto {
            return StudyGroupQueryModelDto(
                id = entity.identifier,
                name = entity.name,
                profileImage = entity.profileImage,
                owner = MemberPreview.of(owner, GroupMemberRole.OWNER),
                noticeBoardId = "",
                description = entity.description,
                recentPost = entity.recentPost?.let { PostPreview.from(it) },
                recentSchedule = entity.recentSchedule?.let { SchedulePreview.from(it) },
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }

        fun from(entity: StudyGroup, owner: Member, noticeBoard: Board): StudyGroupQueryModelDto {
            return StudyGroupQueryModelDto(
                id = entity.identifier,
                name = entity.name,
                profileImage = entity.profileImage,
                owner = MemberPreview.of(owner, GroupMemberRole.OWNER),
                noticeBoardId = noticeBoard.identifier,
                description = entity.description,
                recentPost = entity.recentPost?.let { PostPreview.from(it) },
                recentSchedule = entity.recentSchedule?.let { SchedulePreview.from(it) },
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}