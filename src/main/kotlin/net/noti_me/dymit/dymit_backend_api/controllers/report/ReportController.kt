package net.noti_me.dymit.dymit_backend_api.controllers.report

import net.noti_me.dymit.dymit_backend_api.application.report.ReportService
import net.noti_me.dymit.dymit_backend_api.application.report.dto.ReportCommand
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportResponse
import net.noti_me.dymit.dymit_backend_api.controllers.report.dto.ReportStatusUpdateRequest
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.RestController

/**
 * ReportApi 인터페이스의 구현체
 * 신고 관련 HTTP 요청을 처리합니다.
 */
@RestController
class ReportController(

    private val reportService: ReportService
) : ReportApi {

    override fun createReport(memberInfo: MemberInfo, request: ReportCreateRequest): ReportResponse {
        val command = ReportCommand(
            title = request.title,
            content = request.content,
            resource = request.resource
        )

        val reportDto = reportService.createReport(memberInfo, command)
        return ReportResponse.from(reportDto)
    }

    override fun updateReportStatus(
        memberInfo: MemberInfo,
        reportId: String,
        request: ReportStatusUpdateRequest
    ): ReportResponse {
        val reportObjectId = ObjectId(reportId)
        val reportDto = reportService.updateReportStatus(memberInfo, reportObjectId, request.status)
        return ReportResponse.from(reportDto)
    }

    override fun getReportList(memberInfo: MemberInfo, cursor: String?, size: Int): ListResponse<ReportResponse> {
        val reportDtos = reportService.getReportList(memberInfo, cursor, size)
        val reportResponses = reportDtos.map { ReportResponse.from(it) }

        return ListResponse.of(
            size = size,
            items = reportResponses,
            extractors = buildMap {
                put("cursor"){ it.id }
            }
        )
    }
}
