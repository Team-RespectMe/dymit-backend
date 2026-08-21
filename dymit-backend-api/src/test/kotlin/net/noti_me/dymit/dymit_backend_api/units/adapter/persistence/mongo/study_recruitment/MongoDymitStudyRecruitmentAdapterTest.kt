package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.adapter.out.persistence.mongo

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.out.persistence.mongo.MongoDymitStudyRecruitmentAdapter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.core.query.Query

internal class MongoDymitStudyRecruitmentAdapterTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val converter = createConverter()
    private val adapter = MongoDymitStudyRecruitmentAdapter(mongoTemplate)

    init {
        Given("legacy Dymit 문서의 contact가 문자열일 때") {
            val recruitmentId = ObjectId.get()
            val writerId = ObjectId.get()
            val groupId = ObjectId.get()
            val query = slot<Query>()
            val legacyDocument = Document("_id", recruitmentId)
                .append("_class", DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS)
                .append(
                    "writer",
                    Document("_id", writerId)
                        .append("nickname", "작성자")
                )
                .append("group_id", groupId)
                .append("type", StudyRecruitmentType.DYMIT.name)
                .append("title", "레거시 모집글")
                .append("description", "소개")
                .append("purpose", "목적")
                .append("recruitment_status", DymitStudyRecruitmentStatus.RECRUITING.name)
                .append("target_member", "백엔드")
                .append("study_format", "온라인")
                .append("contact", "https://legacy.example")
                .append("tags", listOf("legacy"))
                .append("isDeleted", false)

            every { mongoTemplate.converter } returns converter
            every {
                mongoTemplate.findOne(
                    capture(query),
                    Document::class.java,
                    "study_recruitments"
                )
            } returns legacyDocument

            When("어댑터가 단건 조회를 수행하면") {
                val result = adapter.loadById(recruitmentId)

                Then("레거시 문자열 contact를 Contact(url,title) 구조로 정규화한다") {
                    verify(exactly = 1) {
                        mongoTemplate.findOne(any(), Document::class.java, "study_recruitments")
                    }
                    query.captured.queryObject["_id"] shouldBe recruitmentId
                    query.captured.queryObject["_class"] shouldBe DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS
                    query.captured.queryObject["type"] shouldBe StudyRecruitmentType.DYMIT
                    result!!.contact shouldBe Contact(
                        url = "https://legacy.example",
                        title = ""
                    )
                }
            }
        }
    }

    private fun createConverter(): MappingMongoConverter {
        val customConversions = MongoCustomConversions(emptyList<Any>())
        val context = MongoMappingContext()
        context.setSimpleTypeHolder(customConversions.simpleTypeHolder)
        context.afterPropertiesSet()

        return MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context).apply {
            setCustomConversions(customConversions)
            afterPropertiesSet()
        }
    }
}
