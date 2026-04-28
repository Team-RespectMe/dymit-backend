package net.noti_me.dymit.dymit_backend_api.application.report.impl

import net.noti_me.dymit.dymit_backend_api.application.report.dto.ReportCommand
import net.noti_me.dymit.dymit_backend_api.application.report.dto.ReportDto
import net.noti_me.dymit.dymit_backend_api.application.report.ReportService
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus
import net.noti_me.dymit.dymit_backend_api.domain.report.Report
import net.noti_me.dymit.dymit_backend_api.ports.persistence.report.ReportRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

/**
 * ReportService의 구현체
 * 신고 생성, 상태 변경, 목록 조회 등의 비즈니스 로직을 처리합니다.
 */
@Service
class ReportServiceImpl(

    private val reportRepository: ReportRepository,
    private val mongoTemplate: MongoTemplate
) : ReportService {

    override fun createReport(memberInfo: MemberInfo, command: ReportCommand): ReportDto {
        val memberObjectId = ObjectId(memberInfo.memberId)

        // 새 신고 생성
        val newReport = Report(
            memberId = memberObjectId,
            resource = command.resource,
            title = command.title,
            content = command.content,
            status = ProcessStatus.REPORTED
        )

        // 신고 저장
        val savedReport = reportRepository.save(newReport)

        return ReportDto.from(savedReport)
    }

    override fun updateReportStatus(memberInfo: MemberInfo, reportId: ObjectId, changeStatus: ProcessStatus): ReportDto {
        // 어드민 권한 확인
        validateAdminRole(memberInfo)

        // 기존 신고 조회
        val existingReport = reportRepository.findById(reportId)
            ?: throw NotFoundException(message="존재하지 않는 신고입니다.")

        // 상태 변경
        existingReport.updateStatus(changeStatus)

        // 변경된 신고 저장
        val updatedReport = reportRepository.save(existingReport)

        return ReportDto.from(updatedReport)
    }

    override fun getReportList(memberInfo: MemberInfo, cursor: String?, size: Int): List<ReportDto> {
        // 어드민 권한 확인
        validateAdminRole(memberInfo)
        val reports = reportRepository.findAllOrderByCreatedAtDesc(cursor, size)

        return reports.map { ReportDto.from(it) }
    }

    /**
     * 어드민 권한을 확인합니다.
     *
     * @param memberInfo 확인할 회원 정보
     * @throws ForbiddenException 어드민 권한이 없는 경우
     */
    private fun validateAdminRole(memberInfo: MemberInfo) {
        if (!memberInfo.roles.contains(MemberRole.ROLE_ADMIN)) {
            throw ForbiddenException(message="어드민 권한이 필요합니다.")
        }
    }
}
