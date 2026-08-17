package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.adapter.out.persistence.mongo

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DYMIT_STUDY_RECURITMENT_TYPE_ALIAS
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecuritment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecuritmentGroup
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecuritmentWriter
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

internal class MongoStudyRecruitmentMappingConverterTest : BehaviorSpec() {

    private val converter = createConverter()

    init {
        Given("a Dymit study recruitment entity") {
            val recruitment = DymitStudyRecuritment(
                id = ObjectId.get(),
                writer = DymitStudyRecuritmentWriter(
                    id = ObjectId.get(),
                    nickname = "작성자"
                ),
                group = DymitStudyRecuritmentGroup(
                    id = ObjectId.get(),
                    name = "테스트 그룹"
                ),
                title = "제목",
                description = "소개",
                purpose = "목적",
                targetMember = "백엔드",
                studyFormat = "온라인",
                contact = "https://example.com/contact"
            )

            When("Spring Data Mongo writes it to a BSON document") {
                val document = Document()
                converter.write(recruitment, document)

                Then("the configured type alias is stored in _class") {
                    document.getString("_class") shouldBe DYMIT_STUDY_RECURITMENT_TYPE_ALIAS
                }
            }
        }

        Given("the study recruitment exclusion query") {
            val queryDocument = Document(
                mapOf(
                    "isDeleted" to false,
                    "_class" to Document("\$ne", DYMIT_STUDY_RECURITMENT_TYPE_ALIAS)
                )
            )

            val legacyDocument = Document(
                mapOf(
                    "_id" to ObjectId.get(),
                    "externalId" to "legacy-1",
                    "isDeleted" to false
                )
            )
            val aliasDocument = Document(
                mapOf(
                    "_id" to ObjectId.get(),
                    "externalId" to "dymit-1",
                    "isDeleted" to false,
                    "_class" to DYMIT_STUDY_RECURITMENT_TYPE_ALIAS
                )
            )

            When("the query is evaluated with Mongo's \$ne semantics") {
                Then("a legacy document without _class remains eligible") {
                    matchesQuery(queryDocument, legacyDocument) shouldBe true
                }

                Then("an alias document is excluded") {
                    matchesQuery(queryDocument, aliasDocument) shouldBe false
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
        val isDeletedMatches = candidate["isDeleted"] == queryDocument["isDeleted"]
        val classCondition = queryDocument["_class"] as Document
        val excludedAlias = classCondition["\$ne"]
        val candidateClass = candidate["_class"]
        val classMatches = candidateClass == null || candidateClass != excludedAlias

        return isDeletedMatches && classMatches
    }
}
