package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import java.time.LocalDateTime

class StudyGroupSummaryDto(
    val id: String,
    val name: String,
    val owner: MemberPreview,
    val description: String,
    val membersCount: Long,
    val inviteCode: InviteCodeVo,
    val createdAt : LocalDateTime
) {
}