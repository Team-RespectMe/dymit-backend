package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_recruitment

import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_recruitment.StudyRecruitmentRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * 스터디 모집 MongoDB 레포지토리 구현체입니다.
 *
 * @property mongoTemplate MongoTemplate
 */
@Repository
class MongoStudyRecruitmentRepository(
    private val mongoTemplate: MongoTemplate
) : StudyRecruitmentRepository {

    /**
     * 커서 기반으로 스터디 모집 목록을 조회합니다.
     *
     * @param cursorId 다음 페이지 조회를 위한 커서 ObjectId
     * @param size 조회 개수
     * @return 스터디 모집 도메인 엔티티 목록
     */
    override fun findByCursorOrderByIdDesc(cursorId: ObjectId?, size: Int): List<StudyRecruitment> {
        val query = Query()
        query.addCriteria(Criteria.where("isDeleted").`is`(false))

        if ( cursorId != null ) {
            query.addCriteria(Criteria.where("_id").lt(cursorId))
        }

        query.limit(size)
        query.with(Sort.by(Sort.Direction.DESC, "_id"))

        return mongoTemplate.find(query, StudyRecruitment::class.java)
    }
}
