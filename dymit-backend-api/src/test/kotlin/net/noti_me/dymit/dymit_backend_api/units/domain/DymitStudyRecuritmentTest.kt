package net.noti_me.dymit.dymit_backend_api.units.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecuritment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecuritmentGroup
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecuritmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecuritmentWriter
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDateTime

internal class DymitStudyRecuritmentTest : BehaviorSpec() {

    init {
        Given("a Dymit study recruitment is created") {
            val writer = DymitStudyRecuritmentWriter(
                id = ObjectId.get(),
                nickname = "작성자"
            )
            val group = DymitStudyRecuritmentGroup(
                id = ObjectId.get(),
                name = "알고리즘 스터디"
            )
            val recruitmentStart = Instant.parse("2026-08-17T00:00:00Z")
            val recruitmentEnd = Instant.parse("2026-08-24T00:00:00Z")

            val recruitment = createRecruitment(
                writer = writer,
                group = group,
                title = "코테 스터디 모집",
                description = "매주 문제 풀이를 진행합니다.",
                purpose = "알고리즘 역량 강화",
                recruitmentStatus = DymitStudyRecuritmentStatus.DONE,
                recruitmentStart = recruitmentStart,
                recruitmentEnd = recruitmentEnd,
                targetMember = "백엔드 개발자",
                studyFormat = "온라인",
                contact = "https://open.kakao.com/o/example"
            )

            When("the aggregate is instantiated") {
                Then("writer, group, and all fields are preserved") {
                    recruitment.writer shouldBe writer
                    recruitment.group shouldBe group
                    recruitment.title shouldBe "코테 스터디 모집"
                    recruitment.description shouldBe "매주 문제 풀이를 진행합니다."
                    recruitment.purpose shouldBe "알고리즘 역량 강화"
                    recruitment.recruitmentStatus shouldBe DymitStudyRecuritmentStatus.DONE
                    recruitment.recruitmentStart shouldBe recruitmentStart
                    recruitment.recruitmentEnd shouldBe recruitmentEnd
                    recruitment.targetMember shouldBe "백엔드 개발자"
                    recruitment.studyFormat shouldBe "온라인"
                    recruitment.contact shouldBe "https://open.kakao.com/o/example"
                }
            }
        }

        Given("field length boundaries") {
            Then("maximum lengths are allowed") {
                createRecruitment(
                    title = "a".repeat(50),
                    description = "b".repeat(200),
                    purpose = "c".repeat(50),
                    targetMember = "d".repeat(100),
                    studyFormat = "e".repeat(100),
                    contact = "f".repeat(255)
                ).title shouldBe "a".repeat(50)
            }

            Then("title longer than 50 characters is rejected") {
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(title = "a".repeat(51))
                }.message shouldBe "제목은(는) 50자 이내로 작성해야 합니다."
            }

            Then("description longer than 200 characters is rejected") {
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(description = "a".repeat(201))
                }.message shouldBe "소개은(는) 200자 이내로 작성해야 합니다."
            }

            Then("purpose longer than 50 characters is rejected") {
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(purpose = "a".repeat(51))
                }.message shouldBe "목적은(는) 50자 이내로 작성해야 합니다."
            }

            Then("target member longer than 100 characters is rejected") {
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(targetMember = "a".repeat(101))
                }.message shouldBe "모집 대상은(는) 100자 이내로 작성해야 합니다."
            }

            Then("study format longer than 100 characters is rejected") {
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(studyFormat = "a".repeat(101))
                }.message shouldBe "운영 방식은(는) 100자 이내로 작성해야 합니다."
            }

            Then("contact longer than 255 characters is rejected") {
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(contact = "a".repeat(256))
                }.message shouldBe "연락처은(는) 255자 이내로 작성해야 합니다."
            }
        }

        Given("the allowed change methods") {
            Then("changeTitle updates only the title and updatedAt") {
                val recruitment = createRecruitment()
                val beforeUpdatedAt = recruitment.updatedAt
                val unchangedDescription = recruitment.description
                val unchangedPurpose = recruitment.purpose
                val unchangedStatus = recruitment.recruitmentStatus
                val unchangedStart = recruitment.recruitmentStart
                val unchangedEnd = recruitment.recruitmentEnd
                val unchangedTargetMember = recruitment.targetMember
                val unchangedStudyFormat = recruitment.studyFormat
                val unchangedContact = recruitment.contact

                recruitment.changeTitle("새 제목")

                recruitment.title shouldBe "새 제목"
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
                recruitment.description shouldBe unchangedDescription
                recruitment.purpose shouldBe unchangedPurpose
                recruitment.recruitmentStatus shouldBe unchangedStatus
                recruitment.recruitmentStart shouldBe unchangedStart
                recruitment.recruitmentEnd shouldBe unchangedEnd
                recruitment.targetMember shouldBe unchangedTargetMember
                recruitment.studyFormat shouldBe unchangedStudyFormat
                recruitment.contact shouldBe unchangedContact
            }

            Then("changeDescription updates only the description and updatedAt") {
                val recruitment = createRecruitment()
                val beforeUpdatedAt = recruitment.updatedAt
                val unchangedTitle = recruitment.title
                val unchangedPurpose = recruitment.purpose

                recruitment.changeDescription("새 소개")

                recruitment.description shouldBe "새 소개"
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
                recruitment.title shouldBe unchangedTitle
                recruitment.purpose shouldBe unchangedPurpose
                recruitment.writer shouldBe recruitment.writer
                recruitment.group shouldBe recruitment.group
            }

            Then("changePurpose updates only the purpose and updatedAt") {
                val recruitment = createRecruitment()
                val beforeUpdatedAt = recruitment.updatedAt
                val unchangedTitle = recruitment.title
                val unchangedDescription = recruitment.description

                recruitment.changePurpose("새 목적")

                recruitment.purpose shouldBe "새 목적"
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
                recruitment.title shouldBe unchangedTitle
                recruitment.description shouldBe unchangedDescription
            }

            Then("changeRecruitmentStart updates only the start time and updatedAt") {
                val recruitment = createRecruitment(recruitmentEnd = Instant.parse("2026-08-24T00:00:00Z"))
                val beforeUpdatedAt = recruitment.updatedAt
                val unchangedEnd = recruitment.recruitmentEnd
                val newStart = Instant.parse("2026-08-18T00:00:00Z")

                recruitment.changeRecruitmentStart(newStart)

                recruitment.recruitmentStart shouldBe newStart
                recruitment.recruitmentEnd shouldBe unchangedEnd
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
            }

            Then("changeRecruitmentEnd updates only the end time and updatedAt") {
                val recruitment = createRecruitment(recruitmentStart = Instant.parse("2026-08-17T00:00:00Z"))
                val beforeUpdatedAt = recruitment.updatedAt
                val unchangedStart = recruitment.recruitmentStart
                val newEnd = Instant.parse("2026-08-25T00:00:00Z")

                recruitment.changeRecruitmentEnd(newEnd)

                recruitment.recruitmentEnd shouldBe newEnd
                recruitment.recruitmentStart shouldBe unchangedStart
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
            }

            Then("changeTargetMember updates only the target member and updatedAt") {
                val recruitment = createRecruitment()
                val beforeUpdatedAt = recruitment.updatedAt
                val unchangedStudyFormat = recruitment.studyFormat
                val unchangedContact = recruitment.contact

                recruitment.changeTargetMember("앱 개발자")

                recruitment.targetMember shouldBe "앱 개발자"
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
                recruitment.studyFormat shouldBe unchangedStudyFormat
                recruitment.contact shouldBe unchangedContact
            }

            Then("changeStudyFormat updates only the study format and updatedAt") {
                val recruitment = createRecruitment()
                val beforeUpdatedAt = recruitment.updatedAt
                val unchangedTargetMember = recruitment.targetMember
                val unchangedContact = recruitment.contact

                recruitment.changeStudyFormat("오프라인")

                recruitment.studyFormat shouldBe "오프라인"
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
                recruitment.targetMember shouldBe unchangedTargetMember
                recruitment.contact shouldBe unchangedContact
            }

            Then("changeContact updates only the contact and updatedAt") {
                val recruitment = createRecruitment()
                val beforeUpdatedAt = recruitment.updatedAt
                val unchangedTargetMember = recruitment.targetMember
                val unchangedStudyFormat = recruitment.studyFormat

                recruitment.changeContact("mailto:test@example.com")

                recruitment.contact shouldBe "mailto:test@example.com"
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
                recruitment.targetMember shouldBe unchangedTargetMember
                recruitment.studyFormat shouldBe unchangedStudyFormat
            }
        }

        Given("recruitment period updates") {
            Then("null start and end values are allowed at creation") {
                val recruitment = createRecruitment(
                    recruitmentStart = null,
                    recruitmentEnd = null
                )

                recruitment.recruitmentStart shouldBe null
                recruitment.recruitmentEnd shouldBe null
            }

            Then("start and end can be changed independently including to null") {
                val start = Instant.parse("2026-08-17T00:00:00Z")
                val end = Instant.parse("2026-08-24T00:00:00Z")
                val recruitment = createRecruitment(
                    recruitmentStart = start,
                    recruitmentEnd = end
                )

                recruitment.changeRecruitmentStart(null)
                recruitment.recruitmentStart shouldBe null
                recruitment.recruitmentEnd shouldBe end

                recruitment.changeRecruitmentEnd(null)
                recruitment.recruitmentEnd shouldBe null
            }
        }

        Given("the aggregate public API") {
            Then("there is no public writer or group change path") {
                val publicMethodNames = DymitStudyRecuritment::class.java.methods.map { it.name }.toSet()

                publicMethodNames.contains("setWriter") shouldBe false
                publicMethodNames.contains("setGroup") shouldBe false
                publicMethodNames.contains("changeWriter") shouldBe false
                publicMethodNames.contains("changeGroup") shouldBe false
            }
        }
    }

    private fun createRecruitment(
        writer: DymitStudyRecuritmentWriter = DymitStudyRecuritmentWriter(
            id = ObjectId.get(),
            nickname = "작성자"
        ),
        group: DymitStudyRecuritmentGroup = DymitStudyRecuritmentGroup(
            id = ObjectId.get(),
            name = "스터디"
        ),
        title: String = "제목",
        description: String = "소개",
        purpose: String = "목적",
        recruitmentStatus: DymitStudyRecuritmentStatus = DymitStudyRecuritmentStatus.RECRUITING,
        recruitmentStart: Instant? = null,
        recruitmentEnd: Instant? = null,
        targetMember: String = "백엔드 개발자",
        studyFormat: String = "온라인",
        contact: String = "https://example.com/contact",
        createdAt: LocalDateTime = LocalDateTime.of(2026, 8, 17, 9, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2026, 8, 17, 9, 0)
    ): DymitStudyRecuritment {
        return DymitStudyRecuritment(
            id = ObjectId.get(),
            writer = writer,
            group = group,
            title = title,
            description = description,
            purpose = purpose,
            recruitmentStatus = recruitmentStatus,
            recruitmentStart = recruitmentStart,
            recruitmentEnd = recruitmentEnd,
            targetMember = targetMember,
            studyFormat = studyFormat,
            contact = contact,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
