package net.noti_me.dymit.dymit_backend_api.ports.persistence.report

import net.noti_me.dymit.dymit_backend_api.domain.report.Report
import org.bson.types.ObjectId

/**
 * 신고(Report) 엔티티에 대한 데이터 접근을 담당하는 레포지토리 인터페이스
 * 신고 정보의 저장, 조회 등의 기능을 제공합니다.
 */
interface ReportRepository {

    /**
     * 신고 정보를 저장합니다.
     *
     * @param report 저장할 신고 엔티티
     * @return 저장된 신고 엔티티, 저장에 실패한 경우 null
     */
    fun save(report: Report): Report

    /**
     * ID로 신고 정보를 조회합니다.
     *
     * @param id 조회할 신고의 ObjectId
     * @return 조회된 신고 엔티티, 존재하지 않는 경우 null
     */
    fun findById(id: ObjectId): Report?


    /**
     * 모든 신고 정보를 최근 시간순(생성일시 내림차순)으로 조회합니다.
     * @param cursor 페이징을 위한 커서 (마지막으로 조회된 항목의 ID), null인 경우 첫 페이지
     * @param size 조회할 신고 개수
     * @return 최근 시간순으로 정렬된 신고 엔티티 목록, 조회에 실패한 경우 빈 목록
     */
    fun findAllOrderByCreatedAtDesc(cursor: String?, size: Int): List<Report>
}
