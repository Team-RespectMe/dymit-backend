package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.adapter.out.persistence.mongo

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.out.persistence.mongo.MongoDymitStudyRecruitmentAdapter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentCursor
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
import java.time.Instant

internal class MongoDymitStudyRecruitmentAdapterTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val converter = createConverter()
    private val adapter = MongoDymitStudyRecruitmentAdapter(mongoTemplate)

    init {
        afterEach {
            clearMocks(mongoTemplate, answers = false, recordedCalls = true)
        }

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

        Given("그룹별 활성 Dymit 공고 존재 여부 조회") {
            val groupId = ObjectId.get()
            val query = slot<Query>()
            every { mongoTemplate.exists(capture(query), "study_recruitments") } returns true

            When("existsActiveByGroupId를 호출하면") {
                val result = adapter.existsActiveByGroupId(groupId)

                Then("alias, type, 미삭제 조건을 유지한 존재 여부 질의를 사용한다") {
                    result shouldBe true
                    query.captured.queryObject["group_id"] shouldBe groupId
                    query.captured.queryObject["_class"] shouldBe DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS
                    query.captured.queryObject["type"] shouldBe StudyRecruitmentType.DYMIT
                    query.captured.queryObject["isDeleted"] shouldBe false
                }
            }
        }

        Given("복합 커서 기반 Dymit 목록 조회") {
            val cursor = DymitStudyRecruitmentCursor(
                bumpAt = Instant.parse("2026-08-21T00:00:00Z"),
                recruitmentId = ObjectId.get()
            )
            val writerId = ObjectId.get()
            val query = slot<Query>()
            every { mongoTemplate.converter } returns converter
            every {
                mongoTemplate.find(
                    capture(query),
                    Document::class.java,
                    "study_recruitments"
                )
            } returns emptyList()

            When("복합 커서로 조회하면") {
                adapter.loadByCursorOrderByBumpAtDesc(cursor, 5, writerId)

                Then("bumpAt 내림차순과 _id 보조 정렬, 커서 OR 조건, 작성자 필터를 사용한다") {
                    val queryObject = query.captured.queryObject
                    query.captured.limit shouldBe 5
                    query.captured.sortObject shouldBe Document("bumpAt", -1).append("_id", -1)
                    queryObject["_class"] shouldBe DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS
                    queryObject["type"] shouldBe StudyRecruitmentType.DYMIT
                    queryObject["isDeleted"] shouldBe false
                    queryObject["writer._id"] shouldBe writerId
                    val orConditions = (queryObject["\$or"] as List<*>)
                    orConditions.size shouldBe 3
                    (orConditions[0] as Document)["bumpAt"] shouldBe Document("\$lt", cursor.bumpAt)
                    val tieBreaker = (orConditions[1] as Document)["\$and"] as List<*>
                    (tieBreaker[0] as Document)["bumpAt"] shouldBe cursor.bumpAt
                    (tieBreaker[1] as Document)["_id"] shouldBe Document("\$lt", cursor.recruitmentId)
                    (orConditions[2] as Document)["bumpAt"] shouldBe Document("\$exists", false)
                }
            }
        }

        Given("레거시 ObjectId 커서 기반 Dymit 목록 조회") {
            val cursorId = ObjectId.get()
            val query = slot<Query>()
            val cursorDocument = Document("_id", cursorId)
                .append("bumpAt", java.util.Date.from(Instant.parse("2026-08-22T00:00:00Z")))
            every { mongoTemplate.converter } returns converter
            every {
                mongoTemplate.findOne(any(), Document::class.java, "study_recruitments")
            } returns cursorDocument
            every {
                mongoTemplate.find(
                    capture(query),
                    Document::class.java,
                    "study_recruitments"
                )
            } returns emptyList()

            When("기존 ObjectId 커서를 사용하면") {
                adapter.loadByCursorOrderByIdDesc(cursorId, 3, null)

                Then("커서 문서를 조회해 bumpAt 기준 조건으로 변환한다") {
                    verify(exactly = 1) {
                        mongoTemplate.findOne(any(), Document::class.java, "study_recruitments")
                    }
                    query.captured.sortObject shouldBe Document("bumpAt", -1).append("_id", -1)
                    (query.captured.queryObject["\$or"] as List<*>).size shouldBe 3
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
