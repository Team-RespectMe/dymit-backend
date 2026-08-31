package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.out.persistence.mongo

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.CheckDymitStudyRecruitmentExistencePort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.LoadDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.SaveDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentCursor
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
import java.time.Instant
import java.util.Date

/**
 * Dymit 스터디 모집글 MongoDB 영속성 어댑터입니다.
 *
 * @property mongoTemplate MongoDB 영속성 도구
 */
@Repository
class MongoDymitStudyRecruitmentAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadDymitStudyRecruitmentPort,
    SaveDymitStudyRecruitmentPort,
    CheckDymitStudyRecruitmentExistencePort {

    /**
     * 그룹의 미삭제 Dymit 모집글 존재 여부를 조회합니다.
     *
     * @param groupId 그룹 ObjectId
     * @return 미삭제 Dymit 모집글이 존재하면 true
     */
    override fun existsActiveByGroupId(groupId: ObjectId): Boolean {
        val query = Query()
            .addCriteria(Criteria.where("group_id").`is`(groupId))
            .addCriteria(Criteria.where("_class").`is`(DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS))
            .addCriteria(Criteria.where("type").`is`(StudyRecruitmentType.DYMIT))
            .addCriteria(Criteria.where("isDeleted").`is`(false))

        return mongoTemplate.exists(query, COLLECTION_NAME)
    }

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
     * Dymit 타입 alias와 출처 유형이 일치하는 미삭제 모집글 목록을 끌어올리기 최신순으로 조회합니다.
     *
     * @param cursorId 다음 페이지 커서 ObjectId
     * @param size 조회 개수
     * @param writerId 작성자 필터 ObjectId
     * @return Dymit 모집글 영속성 DTO 목록
     */
    override fun loadByCursorOrderByIdDesc(
        cursorId: ObjectId?,
        size: Int,
        writerId: ObjectId?
    ): List<DymitStudyRecruitmentPersistenceDto> {
        val cursor = cursorId?.let(::loadLegacyCursor)
        return loadByCursorOrderByBumpAtDesc(cursor, size, writerId)
    }

    /**
     * 복합 커서 뒤의 Dymit 모집글을 끌어올리기 시각과 식별자 내림차순으로 조회합니다.
     *
     * @param cursor 다음 페이지 복합 커서
     * @param size 조회 개수
     * @param writerId 작성자 필터 ObjectId
     * @return Dymit 모집글 영속성 DTO 목록
     */
    override fun loadByCursorOrderByBumpAtDesc(
        cursor: DymitStudyRecruitmentCursor?,
        size: Int,
        writerId: ObjectId?
    ): List<DymitStudyRecruitmentPersistenceDto> {
        val query = Query()
            .addCriteria(Criteria.where("_class").`is`(DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS))
            .addCriteria(Criteria.where("type").`is`(StudyRecruitmentType.DYMIT))
            .addCriteria(Criteria.where("isDeleted").`is`(false))
            .limit(size)
            .with(
                Sort.by(
                    Sort.Order.desc("bumpAt"),
                    Sort.Order.desc("_id")
                )
            )

        if ( cursor != null ) {
            query.addCriteria(createCursorCriteria(cursor))
        }
        if ( writerId != null ) {
            query.addCriteria(Criteria.where("writer._id").`is`(writerId))
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
        val hasStoredBumpAt = containsKey("bumpAt")
        normalizeLegacyContact()
        normalizeLegacyBumpFields()
        val recruitment = mongoTemplate.converter.read(
            DymitStudyRecruitment::class.java,
            this
        )
        return DymitStudyRecruitmentPersistenceDto.from(recruitment).copy(
            hasStoredBumpAt = hasStoredBumpAt
        )
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

    private fun Document.normalizeLegacyBumpFields() {
        if ( !containsKey("bumpAt") ) {
            this["bumpAt"] = getObjectId("_id")?.date?.toInstant() ?: Instant.EPOCH
        }
        if ( !containsKey("bumpCount") ) {
            this["bumpCount"] = 0
        }
    }

    private fun loadLegacyCursor(cursorId: ObjectId): DymitStudyRecruitmentCursor {
        val cursorDocument = mongoTemplate.findOne(
            Query.query(Criteria.where("_id").`is`(cursorId)),
            Document::class.java,
            COLLECTION_NAME
        )
        val storedBumpAt = cursorDocument?.get("bumpAt")
        val bumpAt = when ( storedBumpAt ) {
            is Date -> storedBumpAt.toInstant()
            is Instant -> storedBumpAt
            else -> cursorId.date.toInstant()
        }

        return DymitStudyRecruitmentCursor(
            bumpAt = bumpAt,
            recruitmentId = cursorId,
            hasStoredBumpAt = storedBumpAt != null
        )
    }

    private fun createCursorCriteria(cursor: DymitStudyRecruitmentCursor): Criteria {
        if ( !cursor.hasStoredBumpAt ) {
            return Criteria().andOperator(
                Criteria.where("bumpAt").exists(false),
                Criteria.where("_id").lt(cursor.recruitmentId)
            )
        }

        return Criteria().orOperator(
            Criteria.where("bumpAt").lt(cursor.bumpAt),
            Criteria().andOperator(
                Criteria.where("bumpAt").`is`(cursor.bumpAt),
                Criteria.where("_id").lt(cursor.recruitmentId)
            ),
            Criteria.where("bumpAt").exists(false)
        )
    }

    private companion object {
        const val COLLECTION_NAME = "study_recruitments"
    }
}
