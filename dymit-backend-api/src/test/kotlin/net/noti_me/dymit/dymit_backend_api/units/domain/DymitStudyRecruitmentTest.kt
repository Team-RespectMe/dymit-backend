package net.noti_me.dymit.dymit_backend_api.units.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentWriter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDateTime

internal class DymitStudyRecruitmentTest : BehaviorSpec() {

    init {
        Given("Dymit 모집글 생성") {
            val writer = DymitStudyRecruitmentWriter(ObjectId.get(), "작성자")
            val groupId = ObjectId.get()
            val start = Instant.parse("2026-08-17T00:00:00Z")
            val end = Instant.parse("2026-08-24T00:00:00Z")

            val recruitment = createRecruitment(
                writer = writer,
                groupId = groupId,
                recruitmentStart = start,
                recruitmentEnd = end,
                tags = listOf("kotlin", "backend")
            )

            When("엔티티를 만든다") {
                Then("writer, groupId, type, tags와 모든 필드가 보존된다") {
                    recruitment.writer shouldBe writer
                    recruitment.groupId shouldBe groupId
                    recruitment.type shouldBe StudyRecruitmentType.DYMIT
                    recruitment.title shouldBe "테스트 그룹"
                    recruitment.description shouldBe "소개"
                    recruitment.purpose shouldBe "목적"
                    recruitment.recruitmentStatus shouldBe DymitStudyRecruitmentStatus.RECRUITING
                    recruitment.recruitmentStart shouldBe start
                    recruitment.recruitmentEnd shouldBe end
                    recruitment.targetMember shouldBe "백엔드 개발자"
                    recruitment.studyFormat shouldBe "온라인"
                    recruitment.contact shouldBe "https://example.com/contact"
                    recruitment.tags shouldBe listOf("kotlin", "backend")
                }
            }
        }

        Given("길이 경계값") {
            Then("최대 길이는 허용된다") {
                val recruitment = createRecruitment(
                    title = "a".repeat(50),
                    description = "b".repeat(200),
                    purpose = "c".repeat(50),
                    targetMember = "d".repeat(100),
                    studyFormat = "e".repeat(100),
                    contact = "f".repeat(255)
                )

                recruitment.title shouldBe "a".repeat(50)
            }

            Then("최대 길이를 초과하면 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(title = "a".repeat(51))
                }.message shouldBe "제목은(는) 50자 이내로 작성해야 합니다."
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(description = "a".repeat(201))
                }.message shouldBe "소개은(는) 200자 이내로 작성해야 합니다."
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(purpose = "a".repeat(51))
                }.message shouldBe "목적은(는) 50자 이내로 작성해야 합니다."
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(targetMember = "a".repeat(101))
                }.message shouldBe "모집 대상은(는) 100자 이내로 작성해야 합니다."
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(studyFormat = "a".repeat(101))
                }.message shouldBe "운영 방식은(는) 100자 이내로 작성해야 합니다."
                shouldThrow<IllegalArgumentException> {
                    createRecruitment(contact = "a".repeat(256))
                }.message shouldBe "연락처은(는) 255자 이내로 작성해야 합니다."
            }
        }

        Given("허용된 변경 메서드") {
            Then("대상 필드와 updatedAt만 변경한다") {
                val recruitment = createRecruitment(
                    recruitmentStart = Instant.parse("2026-08-17T00:00:00Z"),
                    recruitmentEnd = Instant.parse("2026-08-24T00:00:00Z"),
                    tags = listOf("기존")
                )

                recruitment.changeDescription("새 소개")
                recruitment.description shouldBe "새 소개"

                recruitment.changePurpose("새 목적")
                recruitment.purpose shouldBe "새 목적"

                recruitment.changeRecruitmentStart(null)
                recruitment.recruitmentStart shouldBe null

                recruitment.changeRecruitmentEnd(Instant.parse("2026-08-25T00:00:00Z"))
                recruitment.recruitmentEnd shouldBe Instant.parse("2026-08-25T00:00:00Z")

                recruitment.changeTargetMember("앱 개발자")
                recruitment.targetMember shouldBe "앱 개발자"

                recruitment.changeStudyFormat("오프라인")
                recruitment.studyFormat shouldBe "오프라인"

                recruitment.changeContact("mailto:test@example.com")
                recruitment.contact shouldBe "mailto:test@example.com"

                recruitment.changeRecruitmentStatus(DymitStudyRecruitmentStatus.DONE)
                recruitment.recruitmentStatus shouldBe DymitStudyRecruitmentStatus.DONE

                recruitment.changeTags(listOf("신규", "태그"))
                recruitment.tags shouldBe listOf("신규", "태그")

                recruitment.updatedAt shouldNotBe LocalDateTime.of(2026, 8, 17, 9, 0)
                recruitment.writer.nickname shouldBe "작성자"
                recruitment.groupId shouldBe recruitment.groupId
                recruitment.type shouldBe StudyRecruitmentType.DYMIT
            }
        }

        Given("삭제 처리") {
            Then("soft delete와 updatedAt 갱신이 적용된다") {
                val recruitment = createRecruitment()
                val beforeUpdatedAt = recruitment.updatedAt

                recruitment.markAsDeleted()

                recruitment.isDeleted shouldBe true
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
            }
        }

        Given("공개 API") {
            Then("writer와 groupId를 바꾸는 공개 경로가 없다") {
                val publicMethodNames = DymitStudyRecruitment::class.java.methods.map { it.name }.toSet()

                publicMethodNames.contains("setWriter") shouldBe false
                publicMethodNames.contains("changeWriter") shouldBe false
                publicMethodNames.contains("setGroupId") shouldBe false
                publicMethodNames.contains("changeGroupId") shouldBe false
            }
        }
    }

    private fun createRecruitment(
        writer: DymitStudyRecruitmentWriter = DymitStudyRecruitmentWriter(ObjectId.get(), "작성자"),
        groupId: ObjectId = ObjectId.get(),
        title: String = "테스트 그룹",
        description: String = "소개",
        purpose: String = "목적",
        recruitmentStatus: DymitStudyRecruitmentStatus = DymitStudyRecruitmentStatus.RECRUITING,
        recruitmentStart: Instant? = null,
        recruitmentEnd: Instant? = null,
        targetMember: String = "백엔드 개발자",
        studyFormat: String = "온라인",
        contact: String = "https://example.com/contact",
        tags: List<String> = emptyList()
    ): DymitStudyRecruitment {
        return DymitStudyRecruitment(
            id = ObjectId.get(),
            writer = writer,
            groupId = groupId,
            title = title,
            description = description,
            purpose = purpose,
            recruitmentStatus = recruitmentStatus,
            recruitmentStart = recruitmentStart,
            recruitmentEnd = recruitmentEnd,
            targetMember = targetMember,
            studyFormat = studyFormat,
            contact = contact,
            tags = tags,
            createdAt = LocalDateTime.of(2026, 8, 17, 9, 0),
            updatedAt = LocalDateTime.of(2026, 8, 17, 9, 0)
        )
    }
}
