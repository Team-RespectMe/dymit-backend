package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.SchedulePreview
import java.time.LocalDateTime

class StudyGroupQueryModelDto(
    val id: String,
    val name: String,
    val profileImage: GroupProfileImageVo,
    val owner: MemberPreview,
    val description: String,
    var schedule: SchedulePreview? = null,
    val createdAt: LocalDateTime
) {
}