package net.noti_me.dymit.dymit_backend_api.controllers.report.dto

import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus

/**
 * 신고 상태 변경 요청 DTO
 * 어드민이 신고 상태를 변경할 때 사용됩니다.
 */
data class ReportStatusUpdateRequest(
    val status: ProcessStatus
)
