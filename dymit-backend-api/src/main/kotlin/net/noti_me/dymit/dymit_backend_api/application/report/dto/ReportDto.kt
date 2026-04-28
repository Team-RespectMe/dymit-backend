package net.noti_me.dymit.dymit_backend_api.application.report.dto

import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus
import net.noti_me.dymit.dymit_backend_api.domain.report.Report
import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource
import java.time.LocalDateTime

/**
 * 신고 정보를 전달하기 위한 데이터 전송 객체(DTO)
 * 도메인 엔티티를 외부로 노출하지 않고 필요한 정보만 전달합니다.
 */
data class ReportDto(
    val id: String,
    val memberId: String,
    val resource: ReportedResource,
    val title: String,
    val content: String,
    val status: ProcessStatus,
    val createdAt: LocalDateTime,
) {

    companion object {
        /**
         * Report 도메인 엔티티를 ReportDto로 변환합니다.
         *
         * @param entity 변환할 Report 도메인 엔티티
         * @return 변환된 ReportDto 객체
         */
        fun from(entity: Report): ReportDto {
            return ReportDto(
                id = entity.identifier,
                memberId = entity.memberId.toHexString(),
                resource = entity.resource,
                title = entity.title,
                content = entity.content,
                status = entity.status,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}
