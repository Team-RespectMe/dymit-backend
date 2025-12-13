package net.noti_me.dymit.dymit_backend_api.controllers.report

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportResponse
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportStatusUpdateRequest

/**
 * 신고 관련 API 인터페이스
 * 신고 생성, 상태 변경, 목록 조회 등의 기능을 제공합니다.
 */
@Tag(name = "신고 API", description = "신고 관련 API")
@SecurityRequirement(name = "bearer-jwt")
interface ReportApi {

    @Operation(method = "POST", summary = "신고 생성", description = "새로운 신고를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "신고 생성 성공")
    fun createReport(memberInfo: MemberInfo, request: ReportCreateRequest): ReportResponse

    @Operation(method="PUT", summary = "신고 상태 변경", description = "신고의 처리 상태를 변경합니다. (어드민 전용)")
    @ApiResponse(responseCode = "200", description = "상태 변경 성공")
    fun updateReportStatus(
        memberInfo: MemberInfo,
        reportId: String,
        request: ReportStatusUpdateRequest
    ): ReportResponse

    @Operation(method = "GET", summary = "신고 목록 조회", description = "신고 목록을 커서 기반 페이징으로 조회합니다. (어드민 전용)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    fun getReportList(
        memberInfo: MemberInfo,
        cursor: String?,
        size: Int
    ): ListResponse<ReportResponse>
}
