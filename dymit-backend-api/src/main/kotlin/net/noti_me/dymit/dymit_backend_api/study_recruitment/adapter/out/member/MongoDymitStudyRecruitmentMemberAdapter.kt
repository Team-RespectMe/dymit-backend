package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.out.member

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.LoadDymitStudyRecruitmentMemberPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.dto.DymitStudyRecruitmentMemberDto
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

/**
 * Dymit 스터디 모집글 전용 회원 조회 포트를 MongoDB에 연결합니다.
 *
 * @property mongoTemplate 회원 문서 조회용 MongoDB 템플릿
 */
@Component
class MongoDymitStudyRecruitmentMemberAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadDymitStudyRecruitmentMemberPort {

    /**
     * 회원의 최신 프로필 이미지 썸네일을 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return 모집글 모듈 전용 회원 DTO, 없으면 null
     */
    override fun loadById(memberId: ObjectId): DymitStudyRecruitmentMemberDto? {
        return mongoTemplate.findById(
            memberId,
            Document::class.java,
            COLLECTION_NAME
        )?.toMemberDto()
    }

    /**
     * 여러 회원의 최신 프로필 이미지 썸네일을 한 번에 조회합니다.
     *
     * @param memberIds 회원 식별자 목록
     * @return 조회된 모집글 모듈 전용 회원 DTO 목록
     */
    override fun loadByIds(
        memberIds: Collection<ObjectId>
    ): List<DymitStudyRecruitmentMemberDto> {
        if ( memberIds.isEmpty() ) {
            return emptyList()
        }

        val query = Query.query(Criteria.where("_id").`in`(memberIds))
        return mongoTemplate.find(query, Document::class.java, COLLECTION_NAME)
            .map { it.toMemberDto() }
    }

    private fun Document.toMemberDto(): DymitStudyRecruitmentMemberDto {
        val memberId = requireNotNull(getObjectId("_id")) {
            "회원 문서에 식별자가 없습니다."
        }
        val profileImage = requireNotNull(get("profileImage", Document::class.java)) {
            "회원 문서에 프로필 이미지가 없습니다."
        }
        val profileImageUrl = requireNotNull(profileImage.getString("thumbnail")) {
            "회원 문서에 프로필 이미지 썸네일이 없습니다."
        }

        return DymitStudyRecruitmentMemberDto(
            id = memberId,
            profileImageUrl = profileImageUrl
        )
    }

    private companion object {
        const val COLLECTION_NAME = "members"
    }
}
