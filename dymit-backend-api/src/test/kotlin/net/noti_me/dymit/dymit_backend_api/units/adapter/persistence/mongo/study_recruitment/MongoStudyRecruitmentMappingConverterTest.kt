package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.adapter.out.persistence.mongo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.StudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.StudyRecruitmentResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentWriter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

internal class MongoStudyRecruitmentMappingConverterTest : BehaviorSpec() {

    private val converter = createConverter()
    private val objectMapper = jacksonObjectMapper()

    init {
        Given("Dymit 모집글을 Mongo 문서로 쓸 때") {
            val recruitment = DymitStudyRecruitment(
                id = ObjectId.get(),
                writer = DymitStudyRecruitmentWriter(ObjectId.get(), "작성자"),
                groupId = ObjectId.get(),
                title = "테스트 그룹",
                description = "소개",
                purpose = "목적",
                targetMember = "백엔드",
                studyFormat = "온라인",
                contact = Contact(
                    url = "https://example.com/contact",
                    title = "오픈채팅"
                )
            )

            When("Spring Data MappingMongoConverter를 사용하면") {
                val document = Document()
                converter.write(recruitment, document)

                Then("_class에 Dymit alias가 기록된다") {
                    document.getString("_class") shouldBe DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS
                }
            }
        }

        Given("legacy INFLEARN Mongo 문서") {
            val legacyDocument = Document(
                mapOf(
                    "_id" to ObjectId.get(),
                    "externalId" to "legacy-1",
                    "type" to "INFLEARN",
                    "title" to "외부 모집글",
                    "content" to "본문",
                    "url" to "https://example.com/legacy",
                    "writer" to "외부 작성자",
                    "isDeleted" to false
                )
            )

            When("StudyRecruitment로 읽으면") {
                val recruitment = converter.read(StudyRecruitment::class.java, legacyDocument)

                Then("INFLEARN enum으로 호환 역직렬화된다") {
                    recruitment.type shouldBe StudyRecruitmentType.INFLEARN
                    recruitment.externalId shouldBe "legacy-1"
                }
            }
        }

        Given("v1 외부 모집글 응답") {
            val response = StudyRecruitmentResponse.from(
                StudyRecruitmentDto(
                    id = "id-1",
                    externalId = "external-1",
                    type = StudyRecruitmentType.INFLEARN,
                    title = "외부 모집글",
                    content = "본문",
                    url = "https://example.com/1",
                    writer = "작성자",
                    createdAt = null,
                    updatedAt = null
                )
            )

            When("JSON으로 직렬화하면") {
                val json = objectMapper.writeValueAsString(response)

                Then("기존 v1 type 값은 INFLEARN 문자열로 유지된다") {
                    json.contains("\"type\":\"INFLEARN\"") shouldBe true
                }
            }
        }

        Given("외부 모집글 제외 쿼리 문서") {
            val queryDocument = Query()
                .addCriteria(Criteria.where("isDeleted").`is`(false))
                .addCriteria(Criteria.where("_class").ne(DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS))
                .queryObject

            Then("_class가 없는 legacy 문서는 매칭된다") {
                matchesQuery(queryDocument, Document("isDeleted", false)) shouldBe true
            }

            Then("alias 문서는 제외된다") {
                matchesQuery(
                    queryDocument,
                    Document(mapOf("isDeleted" to false, "_class" to DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS))
                ) shouldBe false
            }
        }

        Given("legacy Dymit Mongo 문서") {
            val savedDocument = Document()
            converter.write(
                DymitStudyRecruitment(
                    id = ObjectId.get(),
                    writer = DymitStudyRecruitmentWriter(ObjectId.get(), "작성자"),
                    groupId = ObjectId.get(),
                    type = StudyRecruitmentType.DYMIT,
                    title = "기존 모집글",
                    description = "소개",
                    purpose = "목적",
                    targetMember = "백엔드",
                    studyFormat = "온라인",
                    contact = Contact(
                        url = "https://example.com/contact",
                        title = "오픈채팅"
                    ),
                    tags = listOf("kotlin"),
                    isDeleted = false
                ),
                savedDocument
            )
            val legacyDocument = Document().apply {
                putAll(savedDocument)
                remove("_class")
            }

            When("DymitStudyRecruitment로 읽으면") {
                val recruitment = converter.read(DymitStudyRecruitment::class.java, legacyDocument)

                Then("_class가 없어도 type 기반 legacy 문서를 읽을 수 있다") {
                    recruitment.type shouldBe StudyRecruitmentType.DYMIT
                    recruitment.contact shouldBe Contact(
                        url = "https://example.com/contact",
                        title = "오픈채팅"
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

    private fun matchesQuery(
        queryDocument: Document,
        candidate: Document
    ): Boolean {
        val classCondition = queryDocument["_class"] as Document
        val excludedAlias = classCondition["\$ne"]
        val candidateClass = candidate["_class"]

        return candidate["isDeleted"] == queryDocument["isDeleted"] &&
            (candidateClass == null || candidateClass != excludedAlias)
    }
}
