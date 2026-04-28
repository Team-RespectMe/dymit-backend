package net.noti_me.dymit.dymit_backend_api.controllers.report.dto

import net.noti_me.dymit.dymit_backend_api.application.report.dto.ReportDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus
import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource
import java.time.LocalDateTime

/**
 * 신고 정보 응답 DTO
 * 신고 정보를 클라이언트에 반환할 때 사용됩니다.
 */
data class ReportResponse(
    val id: String,
    val memberId: String,
    val resource: ReportedResource,
    val title: String,
    val content: String,
    val status: ProcessStatus,
    val createdAt: LocalDateTime
): BaseResponse() {
    companion object {
        /**
         * ReportDto를 ReportResponse로 변환합니다.
         *
         * @param dto 변환할 ReportDto
         * @return 변환된 ReportResponse
         */
        fun from(dto: ReportDto): ReportResponse {
            return ReportResponse(
                id = dto.id,
                memberId = dto.memberId,
                resource = dto.resource,
                title = dto.title,
                content = dto.content,
                status = dto.status,
                createdAt = dto.createdAt
            )
        }
    }
}
