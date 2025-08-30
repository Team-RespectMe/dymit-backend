package net.noti_me.dymit.dymit_backend_api.controllers.report

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportResponse
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportStatusUpdateRequest
import org.springframework.web.bind.annotation.*

/**
 * 신고 관련 API 인터페이스
 * 신고 생성, 상태 변경, 목록 조회 등의 기능을 제공합니다.
 */
@Tag(name = "신고 API", description = "신고 관련 API")
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearer-jwt")
interface ReportApi {

    @PostMapping("/reports")
    @Operation(summary = "신고 생성", description = "새로운 신고를 생성합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "신고 생성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    ])
    fun createReport(
        @LoginMember memberInfo: MemberInfo,
        @RequestBody request: ReportCreateRequest
    ): ReportResponse

    @PutMapping("/reports/{reportId}/status")
    @Operation(summary = "신고 상태 변경", description = "신고의 처리 상태를 변경합니다. (어드민 전용)")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "상태 변경 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음")
    ])
    fun updateReportStatus(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable reportId: String,
        @RequestBody request: ReportStatusUpdateRequest
    ): ReportResponse

    @GetMapping("/reports")
    @Operation(summary = "신고 목록 조회", description = "신고 목록을 커서 기반 페이징으로 조회합니다. (어드민 전용)")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    ])
    fun getReportList(
        @LoginMember memberInfo: MemberInfo,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int
    ): ListResponse<ReportResponse>
}
