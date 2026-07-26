package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query

import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.InviteCodeVo
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