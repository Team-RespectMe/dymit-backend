package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.report

import net.noti_me.dymit.dymit_backend_api.domain.report.Report
import net.noti_me.dymit.dymit_backend_api.ports.persistence.report.ReportRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * MongoDB를 사용한 ReportRepository 구현체
 * MongoTemplate을 이용하여 신고 정보의 저장, 조회 기능을 제공합니다.
 */
@Repository
class MongoReportRepository(
    private val mongoTemplate: MongoTemplate
) : ReportRepository {

    /**
     * 신고 정보를 MongoDB에 저장합니다.
     *
     * @param report 저장할 신고 엔티티
     * @return 저장된 신고 엔티티
     */
    override fun save(report: Report): Report {
        return mongoTemplate.save(report)
    }

    /**
     * ID로 신고 정보를 MongoDB에서 조회합니다.
     *
     * @param id 조회할 신고의 ObjectId
     * @return 조회된 신고 엔티티, 존재하지 않는 경우 null
     */
    override fun findById(id: ObjectId): Report? {
        return mongoTemplate.findById(id, Report::class.java)
    }

    /**
     * 모든 신고 정보를 최근 시간순(생성일시 내림차순)으로 MongoDB에서 조회합니다.
     *
     * @return 최근 시간순으로 정렬된 신고 엔티티 목록, 조회에 실패한 경우 빈 목록
     */
    override fun findAllOrderByCreatedAtDesc(
        cursor: String?,
        size: Int
    ): List<Report> {
        val query = Query().with(Sort.by(Sort.Direction.DESC, "_id"))

        if (cursor != null) {
            // 커서가 제공된 경우, 해당 커서 이후의 항목부터 조회
            query.addCriteria(Criteria.where("_id").lt(ObjectId(cursor)))
        }
        query.limit(size)

        return mongoTemplate.find(query, Report::class.java)
    }
}
