package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.out.persistence.mongo

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.LoadStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.dto.StudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * 스터디 모집 MongoDB 조회 어댑터입니다.
 *
 * @property mongoTemplate MongoDB 조회 도구
 */
@Repository
class MongoStudyRecruitmentAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadStudyRecruitmentPort {

    /**
     * 커서보다 작은 식별자의 미삭제 모집글을 최신순으로 조회합니다.
     *
     * @param cursorId 다음 페이지 조회를 위한 커서 ObjectId
     * @param size 조회 개수
     * @return 출력 포트용 스터디 모집 정보 목록
     */
    override fun findByCursorOrderByIdDesc(
        cursorId: ObjectId?,
        size: Int
    ): List<StudyRecruitmentPersistenceDto> {
        val query = Query()
            .addCriteria(Criteria.where("isDeleted").`is`(false))
            .addCriteria(Criteria.where("type").`is`(StudyRecruitmentType.INFLEARN))
            .addCriteria(Criteria.where("_class").ne(DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS))
            .limit(size)
            .with(Sort.by(Sort.Direction.DESC, "_id"))

        if ( cursorId != null ) {
            query.addCriteria(Criteria.where("_id").lt(cursorId))
        }

        return mongoTemplate.find(query, StudyRecruitment::class.java)
            .map { it.toPersistenceDto() }
    }

    private fun StudyRecruitment.toPersistenceDto(): StudyRecruitmentPersistenceDto {
        return StudyRecruitmentPersistenceDto(
            id = identifier,
            externalId = requireNotNull(externalId) {
                "외부 스터디 모집글에는 externalId가 필요합니다."
            },
            type = type,
            title = title,
            content = content,
            url = url,
            writer = writer,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
