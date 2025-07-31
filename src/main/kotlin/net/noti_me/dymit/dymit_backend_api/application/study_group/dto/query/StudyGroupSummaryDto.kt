package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import java.time.LocalDateTime

class StudyGroupSummaryDto(
    val id: String,
    val name: String,
    val owner: MemberPreview,
    val description: String,
    val membersCount: Long,
    val createdAt : LocalDateTime
) {
}