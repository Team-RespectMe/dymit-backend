package net.noti_me.dymit.dymit_backend_api.controllers.report.dto

import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource

/**
 * 신고 생성 요청 DTO
 * 클라이언트로부터 신고 생성에 필요한 정보를 받습니다.
 */
data class ReportCreateRequest(
    val title: String,
    val content: String,
    val resource: ReportedResource
)
