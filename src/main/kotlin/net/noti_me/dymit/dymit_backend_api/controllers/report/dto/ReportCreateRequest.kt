package net.noti_me.dymit.dymit_backend_api.controllers.report.dto

import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource
import org.hibernate.validator.constraints.Length

/**
 * 신고 생성 요청 DTO
 * 클라이언트로부터 신고 생성에 필요한 정보를 받습니다.
 */
@Sanitize
data class ReportCreateRequest(
    @Length(max = 100, message = "신고 제목은 최대 100자까지 입력할 수 있습니다.")
    val title: String,
    @Length(max = 500, message = "신고 내용은 최대 500자까지 입력할 수 있습니다.")
    val content: String,
    val resource: ReportedResource
)
