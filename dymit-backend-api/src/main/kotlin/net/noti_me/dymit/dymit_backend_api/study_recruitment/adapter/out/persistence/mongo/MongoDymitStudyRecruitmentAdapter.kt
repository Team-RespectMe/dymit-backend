package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.out.persistence.mongo

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.LoadDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.SaveDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * Dymit 스터디 모집글 MongoDB 영속성 어댑터입니다.
 *
 * @property mongoTemplate MongoDB 영속성 도구
 */
@Repository
class MongoDymitStudyRecruitmentAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadDymitStudyRecruitmentPort, SaveDymitStudyRecruitmentPort {

    /**
     * Dymit 타입 alias와 출처 유형이 일치하는 미삭제 모집글만 단건 조회합니다.
     *
     * @param recruitmentId 모집글 ObjectId
     * @return Dymit 모집글 영속성 DTO 또는 null
     */
    override fun loadById(recruitmentId: ObjectId): DymitStudyRecruitmentPersistenceDto? {
        val query = Query()
            .addCriteria(Criteria.where("_id").`is`(recruitmentId))
            .addCriteria(Criteria.where("_class").`is`(DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS))
            .addCriteria(Criteria.where("type").`is`(StudyRecruitmentType.DYMIT))
            .addCriteria(Criteria.where("isDeleted").`is`(false))

        return mongoTemplate.findOne(
            query,
            Document::class.java,
            COLLECTION_NAME
        )?.toPersistenceDto()
    }

    /**
     * Dymit 타입 alias와 출처 유형이 일치하는 미삭제 모집글 목록을 최신순으로 조회합니다.
     *
     * @param cursorId 다음 페이지 커서 ObjectId
     * @param size 조회 개수
     * @return Dymit 모집글 영속성 DTO 목록
     */
    override fun loadByCursorOrderByIdDesc(
        cursorId: ObjectId?,
        size: Int
    ): List<DymitStudyRecruitmentPersistenceDto> {
        val query = Query()
            .addCriteria(Criteria.where("_class").`is`(DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS))
            .addCriteria(Criteria.where("type").`is`(StudyRecruitmentType.DYMIT))
            .addCriteria(Criteria.where("isDeleted").`is`(false))
            .limit(size)
            .with(Sort.by(Sort.Direction.DESC, "_id"))

        if ( cursorId != null ) {
            query.addCriteria(Criteria.where("_id").lt(cursorId))
        }

        return mongoTemplate.find(query, Document::class.java, COLLECTION_NAME)
            .map { it.toPersistenceDto() }
    }

    /**
     * DYMIT 출처 모집글만 저장합니다.
     *
     * @param recruitment 저장할 Dymit 모집글 엔티티
     * @return 저장된 모집글 영속성 DTO
     */
    override fun save(recruitment: DymitStudyRecruitment): DymitStudyRecruitmentPersistenceDto {
        require(recruitment.type == StudyRecruitmentType.DYMIT) {
            "Dymit 모집글은 DYMIT 유형으로만 저장할 수 있습니다."
        }
        return DymitStudyRecruitmentPersistenceDto.from(mongoTemplate.save(recruitment))
    }

    private fun Document.toPersistenceDto(): DymitStudyRecruitmentPersistenceDto {
        normalizeLegacyContact()
        val recruitment = mongoTemplate.converter.read(
            DymitStudyRecruitment::class.java,
            this
        )
        return DymitStudyRecruitmentPersistenceDto.from(recruitment)
    }

    private fun Document.normalizeLegacyContact() {
        val legacyContact = get("contact") as? String ?: return
        this["contact"] = Document(
            mapOf(
                "url" to legacyContact,
                "title" to ""
            )
        )
    }

    private companion object {
        const val COLLECTION_NAME = "study_recruitments"
    }
}
