package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.out.study_group

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group.DymitStudyRecruitmentLoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group.dto.DymitStudyRecruitmentStudyGroupDto
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * Dymit 모집글에 필요한 스터디 그룹 정보를 MongoDB에서 조회하는 어댑터입니다.
 *
 * @property mongoTemplate MongoDB 조회 도구
 */
@Repository
class MongoDymitStudyRecruitmentLoadStudyGroupAdapter(
    private val mongoTemplate: MongoTemplate
) : DymitStudyRecruitmentLoadStudyGroupPort {

    /**
     * 스터디 그룹 도메인을 직접 참조하지 않고 필요한 그룹 정보만 조회합니다.
     *
     * @param groupId 그룹 ObjectId
     * @return 모집글용 그룹 DTO 또는 null
     */
    override fun loadById(groupId: ObjectId): DymitStudyRecruitmentStudyGroupDto? {
        val query = Query()
            .addCriteria(Criteria.where("_id").`is`(groupId))
            .addCriteria(Criteria.where("isDeleted").`is`(false))
        val group = mongoTemplate.findOne(query, Document::class.java, STUDY_GROUP_COLLECTION)
            ?: return null

        return DymitStudyRecruitmentStudyGroupDto(
            id = group.getObjectId("_id"),
            ownerId = group.getObjectId("ownerId"),
            name = group.getString("name")
        )
    }

    private companion object {

        const val STUDY_GROUP_COLLECTION = "study_groups"
    }
}
