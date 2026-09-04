package net.noti_me.dymit.dymit_backend_api.units.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.common.errors.TooManyRequestException
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentWriter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import java.time.Instant


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
                    recruitment.contact shouldBe Contact(
                        url = "https://example.com/contact",
                        title = "오픈채팅"
                    )
                    recruitment.tags shouldBe listOf("kotlin", "backend")
                    recruitment.bumpCount shouldBe 0
                    recruitment.bumpAt shouldNotBe null
                }
            }
        }

        Given("끌어올리기") {
            Then("1회 호출 시 횟수가 증가하고 시간이 갱신된다") {
                val recruitment = createRecruitment()
                val beforeBumpAt = recruitment.bumpAt
                val beforeUpdatedAt = recruitment.updatedAt

                recruitment.bump()

                recruitment.bumpCount shouldBe 1
                recruitment.bumpAt shouldNotBe beforeBumpAt
                recruitment.updatedAt shouldNotBe beforeUpdatedAt
            }

            Then("5회까지는 성공하고 6회째는 제한 예외를 던진다") {
                val recruitment = createRecruitment()

                repeat(5) {
                    recruitment.bump()
                }

                recruitment.bumpCount shouldBe 5
                val exception = shouldThrow<TooManyRequestException> {
                    recruitment.bump()
                }
                exception.code shouldBe "EXCEED_BUMP_COUNT"
                exception.message shouldBe "끌어올리기 최대 횟수를 초과하였습니다."
            }
        }

        Given("Contact 값 객체") {
            Then("url과 title을 보존한다") {
                Contact(
                    url = "https://example.com/contact",
                    title = "문의 링크"
                ) shouldBe Contact(
                    url = "https://example.com/contact",
                    title = "문의 링크"
                )
            }

            Then("url 길이가 255자를 초과하면 거부된다") {
                shouldThrow<IllegalArgumentException> {
                    Contact(
                        url = "a".repeat(256),
                        title = "문의"
                    )
                }.message shouldBe "연락 URL은 255자 이내로 작성해야 합니다."
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
                    contact = Contact(
                        url = "f".repeat(255),
                        title = "문의"
                    )
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
                    createRecruitment(contact = Contact(url = "a".repeat(256), title = "문의"))
                }.message shouldBe "연락 URL은 255자 이내로 작성해야 합니다."
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

                val updatedContact = Contact(
                    url = "mailto:test@example.com",
                    title = "이메일"
                )
                recruitment.changeContact(updatedContact)
                recruitment.contact shouldBe updatedContact

                recruitment.changeRecruitmentStatus(DymitStudyRecruitmentStatus.DONE)
                recruitment.recruitmentStatus shouldBe DymitStudyRecruitmentStatus.DONE

                recruitment.changeTags(listOf("신규", "태그"))
                recruitment.tags shouldBe listOf("신규", "태그")

                recruitment.updatedAt shouldNotBe Instant.parse("2026-08-17T09:00:00Z")
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
        contact: Contact = Contact(
            url = "https://example.com/contact",
            title = "오픈채팅"
        ),
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
            createdAt = Instant.parse("2026-08-17T09:00:00Z"),
            updatedAt = Instant.parse("2026-08-17T09:00:00Z")
        )
    }
}
