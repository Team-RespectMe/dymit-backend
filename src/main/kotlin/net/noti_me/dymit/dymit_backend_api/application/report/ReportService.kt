package net.noti_me.dymit.dymit_backend_api.application.report

import net.noti_me.dymit.dymit_backend_api.application.report.dto.ReportCommand
import net.noti_me.dymit.dymit_backend_api.application.report.dto.ReportDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus
import org.bson.types.ObjectId

/**
 * 신고 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 * 유저의 신고 생성, 어드민의 신고 상태 변경, 신고 목록 조회 기능을 제공합니다.
 */
interface ReportService {

    /**
     * 새로운 신고를 생성합니다. (유저에 의한)
     *
     * @param memberInfo 신고를 생성하는 회원 정보
     * @param command 신고 생성에 필요한 정보 (제목, 내용, 리소스 정보)
     * @return 생성된 신고 정보 DTO
     */
    fun createReport(memberInfo: MemberInfo, command: ReportCommand): ReportDto

    /**
     * 기존 신고의 상태를 변경합니다. (어드민에 의한)
     *
     * @param memberInfo 상태를 변경하는 어드민 회원 정보
     * @param reportId 상태를 변경할 신고의 ID
     * @param changeStatus 변경할 처리 상태
     * @return 상태가 변경된 신고 정보 DTO
     */
    fun updateReportStatus(memberInfo: MemberInfo, reportId: ObjectId, changeStatus: ProcessStatus): ReportDto

    /**
     * 신고 목록을 조회합니다. (어드민만 가능)
     * 커서 기반 페이징을 통해 신고 목록을 최신 순으로 조회합니다.
     *
     * @param memberInfo 목록을 조회하는 어드민 회원 정보
     * @param cursor 페이징을 위한 커서 (마지막으로 조회된 항목의 ID), null인 경우 첫 페이지
     * @param size 조회할 신고 개수
     * @return 신고 정보 DTO 목록
     */
    fun getReportList(memberInfo: MemberInfo, cursor: String?, size: Int): List<ReportDto>
}
