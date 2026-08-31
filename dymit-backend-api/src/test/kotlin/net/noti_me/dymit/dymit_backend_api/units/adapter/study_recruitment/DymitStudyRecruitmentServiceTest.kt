package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.BumpStudyRecruitmentService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.CreateDymitStudyRecruitmentService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.DeleteDymitStudyRecruitmentService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.GetDymitStudyRecruitmentListService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.GetDymitStudyRecruitmentService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.UpdateDymitStudyRecruitmentService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.BumpStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CreateDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DeleteDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentSummaryDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentListQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.UpdateDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.CheckDymitStudyRecruitmentExistencePort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.LoadDymitStudyRecruitmentMemberPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.dto.DymitStudyRecruitmentMemberDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.LoadDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.SaveDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentCursor
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group.DymitStudyRecruitmentLoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group.dto.DymitStudyRecruitmentStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentWriter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDateTime

internal class DymitStudyRecruitmentServiceTest : BehaviorSpec() {

    private val loadStudyGroupPort = mockk<DymitStudyRecruitmentLoadStudyGroupPort>()
    private val saveRecruitmentPort = mockk<SaveDymitStudyRecruitmentPort>()
    private val loadRecruitmentPort = mockk<LoadDymitStudyRecruitmentPort>()
    private val loadMemberPort = mockk<LoadDymitStudyRecruitmentMemberPort>()
    private val checkRecruitmentExistencePort = mockk<CheckDymitStudyRecruitmentExistencePort>()

    private val createService = CreateDymitStudyRecruitmentService(
        loadStudyGroupPort,
        saveRecruitmentPort,
        loadMemberPort,
        checkRecruitmentExistencePort
    )
    private val getService = GetDymitStudyRecruitmentService(loadRecruitmentPort, loadMemberPort)
    private val getListService = GetDymitStudyRecruitmentListService(loadRecruitmentPort)
    private val updateService = UpdateDymitStudyRecruitmentService(loadRecruitmentPort, loadStudyGroupPort, saveRecruitmentPort, loadMemberPort)
    private val deleteService = DeleteDymitStudyRecruitmentService(loadRecruitmentPort, loadStudyGroupPort, saveRecruitmentPort)
    private val bumpService = BumpStudyRecruitmentService(loadRecruitmentPort, loadStudyGroupPort, saveRecruitmentPort, loadMemberPort)

    private val ownerId = ObjectId.get()
    private val memberInfo = MemberInfo(
        memberId = ownerId.toHexString(),
        nickname = "owner",
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )
    private val groupDto = DymitStudyRecruitmentStudyGroupDto(
        id = ObjectId.get(),
        ownerId = ownerId,
        name = "테스트 그룹"
    )
    private val writerMemberDto = DymitStudyRecruitmentMemberDto(
        id = ownerId,
        profileImageUrl = "https://example.com/profile-thumb.png"
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("Dymit 생성 서비스") {
            val command = CreateDymitStudyRecruitmentCommand(
                groupId = groupDto.id.toHexString(),
                title = "커맨드 제목",
                description = "소개",
                purpose = "목적",
                targetMember = "백엔드",
                studyFormat = "온라인",
                contact = Contact(
                    url = "https://example.com/contact",
                    title = "오픈채팅"
                ),
                recruitmentStart = Instant.parse("2026-08-17T00:00:00Z"),
                recruitmentEnd = Instant.parse("2026-08-24T00:00:00Z"),
                tags = emptyList()
            )

            Then("생성 시 command title과 writer, DYMIT type, 기본 tags를 저장하고 그룹 소유자만 허용한다") {
                val captured = slot<DymitStudyRecruitment>()
                every { checkRecruitmentExistencePort.existsActiveByGroupId(groupDto.id) } returns false
                every { loadStudyGroupPort.loadById(groupDto.id) } returns groupDto
                every { saveRecruitmentPort.save(capture(captured)) } answers { persistenceDtoFrom(captured.captured) }
                every { loadMemberPort.loadById(ownerId) } returns writerMemberDto

                val result = createService.execute(memberInfo, command)

                captured.captured.type shouldBe StudyRecruitmentType.DYMIT
                captured.captured.title shouldBe "커맨드 제목"
                captured.captured.writer shouldBe DymitStudyRecruitmentWriter(ownerId, "owner")
                captured.captured.groupId shouldBe groupDto.id
                captured.captured.tags shouldBe emptyList()
                result.groupId shouldBe groupDto.id.toHexString()
                result.type shouldBe StudyRecruitmentType.DYMIT
                result.writerNickname shouldBe "owner"
                result.writerProfileImageUrl shouldBe "https://example.com/profile-thumb.png"
                result.contact shouldBe command.contact
            }

            Then("그룹 소유자가 아니면 생성할 수 없다") {
                every { checkRecruitmentExistencePort.existsActiveByGroupId(groupDto.id) } returns false
                every { loadStudyGroupPort.loadById(groupDto.id) } returns groupDto

                shouldThrow<ForbiddenException> {
                    createService.execute(memberInfo = MemberInfo(ObjectId.get().toHexString(), "other", memberInfo.roles), command = command)
                }.message shouldBe "그룹 소유자만 모집글을 생성할 수 있습니다."
            }

            Then("동일 그룹의 미삭제 모집글이 이미 있으면 ConflictException을 던지고 저장하지 않는다") {
                every { checkRecruitmentExistencePort.existsActiveByGroupId(groupDto.id) } returns true

                shouldThrow<ConflictException> {
                    createService.execute(memberInfo, command)
                }.message shouldBe "해당 스터디 그룹의 모집 공고가 이미 존재합니다."

                verify(exactly = 0) { loadStudyGroupPort.loadById(any()) }
                verify(exactly = 0) { saveRecruitmentPort.save(any()) }
            }

            Then("작성자 회원 정보가 없으면 실패한다") {
                val captured = slot<DymitStudyRecruitment>()
                every { checkRecruitmentExistencePort.existsActiveByGroupId(groupDto.id) } returns false
                every { loadStudyGroupPort.loadById(groupDto.id) } returns groupDto
                every { saveRecruitmentPort.save(capture(captured)) } answers { persistenceDtoFrom(captured.captured) }
                every { loadMemberPort.loadById(ownerId) } returns null

                shouldThrow<NotFoundException> {
                    createService.execute(memberInfo, command)
                }.message shouldBe "존재하지 않는 작성자입니다."
            }
        }

        Given("Dymit 단건 조회 서비스") {
            val recruitmentId = ObjectId.get()
            every { loadRecruitmentPort.loadById(recruitmentId) } returns createPersistenceDto(recruitmentId = recruitmentId)
            every { loadMemberPort.loadById(ownerId) } returns writerMemberDto

            Then("GetDymitStudyRecruitmentQuery로 단건을 조회한다") {
                val result = getService.execute(GetDymitStudyRecruitmentQuery(recruitmentId.toHexString()))

                verify(exactly = 1) { loadRecruitmentPort.loadById(recruitmentId) }
                result.id shouldBe recruitmentId.toHexString()
                result.writerNickname shouldBe "owner"
                result.writerProfileImageUrl shouldBe "https://example.com/profile-thumb.png"
            }

            Then("존재하지 않으면 NotFoundException을 던진다") {
                every { loadRecruitmentPort.loadById(ObjectId("000000000000000000000099")) } returns null

                shouldThrow<NotFoundException> {
                    getService.execute(GetDymitStudyRecruitmentQuery("000000000000000000000099"))
                }.message shouldBe "존재하지 않는 Dymit 스터디 모집글입니다."
            }

            Then("작성자 회원 정보가 없으면 NotFoundException을 던진다") {
                every { loadRecruitmentPort.loadById(recruitmentId) } returns createPersistenceDto(recruitmentId = recruitmentId)
                every { loadMemberPort.loadById(ownerId) } returns null

                shouldThrow<NotFoundException> {
                    getService.execute(GetDymitStudyRecruitmentQuery(recruitmentId.toHexString()))
                }.message shouldBe "존재하지 않는 작성자입니다."
            }
        }

        Given("Dymit 목록 조회 서비스") {
            val cursor = ObjectId.get()
            every { loadRecruitmentPort.loadByCursorOrderByIdDesc(cursor, 3, null) } returns listOf(
                createPersistenceDto(recruitmentId = ObjectId.get()),
                createPersistenceDto(recruitmentId = ObjectId.get())
            )

            Then("최신순 커서 조회에 size+1을 사용하고 summary만 반환한다") {
                val result = getListService.execute(GetDymitStudyRecruitmentListQuery(cursor.toHexString(), 2))

                verify(exactly = 1) { loadRecruitmentPort.loadByCursorOrderByIdDesc(cursor, 3, null) }
                verify(exactly = 0) { loadMemberPort.loadByIds(any()) }
                verify(exactly = 0) { loadMemberPort.loadById(any()) }
                result.size shouldBe 2
                result.all { it is DymitStudyRecruitmentSummaryDto } shouldBe true
                result.first().title shouldBe "테스트 그룹"
                result.first().purpose shouldBe "목적"
                result.first().writerId shouldBe ownerId.toHexString()
                result.first().tags shouldBe emptyList()
                result.first().type shouldBe StudyRecruitmentType.DYMIT
                result.first().status shouldBe DymitStudyRecruitmentStatus.RECRUITING
                result.first().content shouldBe "소개"
                result.first().url shouldBe null
            }

            Then("size가 범위를 벗어나면 BadRequestException을 던진다") {
                shouldThrow<BadRequestException> {
                    getListService.execute(GetDymitStudyRecruitmentListQuery(size = 0))
                }.message shouldBe "조회 크기는 1 이상 100 이하여야 합니다."
            }

            Then("목록 조회는 작성자 조회 포트를 호출하지 않는다") {
                every { loadRecruitmentPort.loadByCursorOrderByIdDesc(cursor, 3, null) } returns listOf(
                    createPersistenceDto(recruitmentId = ObjectId.get()),
                    createPersistenceDto(recruitmentId = ObjectId.get())
                )

                getListService.execute(GetDymitStudyRecruitmentListQuery(cursor.toHexString(), 2))

                verify(exactly = 0) { loadMemberPort.loadByIds(any()) }
                verify(exactly = 0) { loadMemberPort.loadById(any()) }
            }

            Then("mine=true 이면 요청 회원 ID를 작성자 필터로 전달한다") {
                every { loadRecruitmentPort.loadByCursorOrderByIdDesc(cursor, 3, ownerId) } returns listOf(
                    createPersistenceDto(recruitmentId = ObjectId.get())
                )

                getListService.execute(
                    GetDymitStudyRecruitmentListQuery(
                        cursor = cursor.toHexString(),
                        size = 2,
                        mine = true,
                        memberId = ownerId.toHexString()
                    )
                )

                verify(exactly = 1) { loadRecruitmentPort.loadByCursorOrderByIdDesc(cursor, 3, ownerId) }
            }

            Then("mine=true 이고 회원 식별자가 올바르지 않으면 BadRequestException을 던진다") {
                shouldThrow<BadRequestException> {
                    getListService.execute(
                        GetDymitStudyRecruitmentListQuery(
                            size = 2,
                            mine = true,
                            memberId = "invalid-member-id"
                        )
                    )
                }.message shouldBe "올바르지 않은 회원 식별자입니다."
            }

            Then("복합 커서를 받으면 bumpAt 기준 조회 포트를 호출한다") {
                val compoundCursor = DymitStudyRecruitmentCursor(
                    bumpAt = Instant.parse("2026-08-21T00:00:00Z"),
                    recruitmentId = ObjectId.get()
                )
                every {
                    loadRecruitmentPort.loadByCursorOrderByBumpAtDesc(compoundCursor, 3, null)
                } returns listOf(createPersistenceDto(recruitmentId = ObjectId.get()))

                getListService.execute(GetDymitStudyRecruitmentListQuery(compoundCursor.encode(), 2))

                verify(exactly = 1) {
                    loadRecruitmentPort.loadByCursorOrderByBumpAtDesc(compoundCursor, 3, null)
                }
            }
        }

        Given("Dymit 끌어올리기 서비스") {
            val recruitmentId = ObjectId.get()
            val command = BumpStudyRecruitmentCommand(recruitmentId.toHexString())
            val persistenceDto = createPersistenceDto(recruitmentId = recruitmentId)

            Then("유효한 공고면 조회 후 bump 및 저장 후 작성자 조회를 거쳐 DTO를 반환한다") {
                val captured = slot<DymitStudyRecruitment>()
                every { loadRecruitmentPort.loadById(recruitmentId) } returns persistenceDto
                every { loadStudyGroupPort.loadById(groupDto.id) } returns groupDto
                every { saveRecruitmentPort.save(capture(captured)) } answers { persistenceDtoFrom(captured.captured) }
                every { loadMemberPort.loadById(ownerId) } returns writerMemberDto

                val result = bumpService.execute(memberInfo, command)

                verify(ordering = io.mockk.Ordering.SEQUENCE) {
                    loadRecruitmentPort.loadById(recruitmentId)
                    loadStudyGroupPort.loadById(groupDto.id)
                    saveRecruitmentPort.save(any())
                    loadMemberPort.loadById(ownerId)
                }
                captured.captured.bumpCount shouldBe 1
                result.bumpCount shouldBe 1
                result.id shouldBe recruitmentId.toHexString()
            }

            Then("식별자가 올바르지 않으면 BadRequestException을 던진다") {
                shouldThrow<BadRequestException> {
                    bumpService.execute(memberInfo, BumpStudyRecruitmentCommand("invalid-id"))
                }.message shouldBe "올바르지 않은 모집글 식별자입니다."
            }

            Then("공고가 없으면 NotFoundException을 던진다") {
                every { loadRecruitmentPort.loadById(recruitmentId) } returns null

                shouldThrow<NotFoundException> {
                    bumpService.execute(memberInfo, command)
                }.message shouldBe "존재하지 않는 Dymit 스터디 모집글입니다."
            }

            Then("그룹 소유자가 아니면 ForbiddenException을 던진다") {
                every { loadRecruitmentPort.loadById(recruitmentId) } returns persistenceDto
                every { loadStudyGroupPort.loadById(groupDto.id) } returns groupDto

                shouldThrow<ForbiddenException> {
                    bumpService.execute(
                        memberInfo = MemberInfo(ObjectId.get().toHexString(), "other", memberInfo.roles),
                        command = command
                    )
                }.message shouldBe "그룹 소유자만 모집글을 끌어올릴 수 있습니다."
            }
        }

        Given("Dymit 수정 서비스") {
            val recruitmentId = ObjectId.get()
            val persistenceDto = createPersistenceDto(recruitmentId = recruitmentId)
            val command = UpdateDymitStudyRecruitmentCommand(
                recruitmentId = recruitmentId.toHexString(),
                title = "수정 제목",
                description = "수정 소개",
                purpose = "수정 목적",
                targetMember = "앱 개발자",
                studyFormat = "오프라인",
                contact = Contact(
                    url = "mailto:test@example.com",
                    title = "이메일"
                ),
                recruitmentStart = null,
                recruitmentEnd = Instant.parse("2026-08-25T00:00:00Z"),
                status = DymitStudyRecruitmentStatus.DONE,
                tags = listOf("updated")
            )

            Then("그룹 소유자만 수정할 수 있고 변경 내용을 저장한다") {
                val captured = slot<DymitStudyRecruitment>()
                every { loadRecruitmentPort.loadById(recruitmentId) } returns persistenceDto
                every { loadStudyGroupPort.loadById(groupDto.id) } returns groupDto
                every { saveRecruitmentPort.save(capture(captured)) } answers { persistenceDtoFrom(captured.captured) }
                every { loadMemberPort.loadById(ownerId) } returns writerMemberDto

                val result = updateService.execute(memberInfo, command)

                verify(exactly = 1) { loadRecruitmentPort.loadById(recruitmentId) }
                captured.captured.title shouldBe "수정 제목"
                captured.captured.description shouldBe "수정 소개"
                captured.captured.recruitmentStatus shouldBe DymitStudyRecruitmentStatus.DONE
                captured.captured.tags shouldBe listOf("updated")
                result.contact shouldBe command.contact
                result.writerProfileImageUrl shouldBe "https://example.com/profile-thumb.png"
            }

            Then("외부 모집글은 수정 대상이 아니다") {
                every { loadRecruitmentPort.loadById(recruitmentId) } returns null

                shouldThrow<NotFoundException> {
                    updateService.execute(memberInfo, command)
                }.message shouldBe "존재하지 않는 Dymit 스터디 모집글입니다."
            }

            Then("수정 후 작성자 회원 정보가 없으면 NotFoundException을 던진다") {
                every { loadRecruitmentPort.loadById(recruitmentId) } returns persistenceDto
                every { loadStudyGroupPort.loadById(groupDto.id) } returns groupDto
                every { saveRecruitmentPort.save(any()) } returns persistenceDto
                every { loadMemberPort.loadById(ownerId) } returns null

                shouldThrow<NotFoundException> {
                    updateService.execute(memberInfo, command)
                }.message shouldBe "존재하지 않는 작성자입니다."
            }
        }

        Given("Dymit 삭제 서비스") {
            val recruitmentId = ObjectId.get()
            val persistenceDto = createPersistenceDto(recruitmentId = recruitmentId)

            Then("그룹 소유자만 soft delete 할 수 있다") {
                val captured = slot<DymitStudyRecruitment>()
                every { loadRecruitmentPort.loadById(recruitmentId) } returns persistenceDto
                every { loadStudyGroupPort.loadById(groupDto.id) } returns groupDto
                every { saveRecruitmentPort.save(capture(captured)) } answers { persistenceDtoFrom(captured.captured) }

                deleteService.execute(memberInfo, DeleteDymitStudyRecruitmentCommand(recruitmentId.toHexString()))

                captured.captured.isDeleted shouldBe true
                captured.isCaptured shouldBe true
            }

            Then("외부 모집글은 삭제 대상이 아니다") {
                every { loadRecruitmentPort.loadById(recruitmentId) } returns null

                shouldThrow<NotFoundException> {
                    deleteService.execute(memberInfo, DeleteDymitStudyRecruitmentCommand(recruitmentId.toHexString()))
                }.message shouldBe "존재하지 않는 Dymit 스터디 모집글입니다."
            }
        }
    }

    private fun createPersistenceDto(recruitmentId: ObjectId): DymitStudyRecruitmentPersistenceDto {
        return DymitStudyRecruitmentPersistenceDto(
            id = recruitmentId,
            writerId = ownerId,
            writerNickname = "owner",
            groupId = groupDto.id,
            type = StudyRecruitmentType.DYMIT,
            title = "테스트 그룹",
            description = "소개",
            purpose = "목적",
            recruitmentStatus = DymitStudyRecruitmentStatus.RECRUITING,
            recruitmentStart = Instant.parse("2026-08-17T00:00:00Z"),
            recruitmentEnd = Instant.parse("2026-08-24T00:00:00Z"),
            targetMember = "백엔드",
            studyFormat = "온라인",
            contact = Contact(
                url = "https://example.com/contact",
                title = "오픈채팅"
            ),
            tags = emptyList(),
            createdAt = LocalDateTime.of(2026, 8, 17, 9, 0),
            updatedAt = LocalDateTime.of(2026, 8, 17, 9, 0),
            isDeleted = false,
            bumpAt = Instant.parse("2026-08-17T00:00:00Z"),
            bumpCount = 0
        )
    }

    private fun persistenceDtoFrom(recruitment: DymitStudyRecruitment): DymitStudyRecruitmentPersistenceDto {
        return DymitStudyRecruitmentPersistenceDto(
            id = recruitment.id ?: ObjectId.get(),
            writerId = recruitment.writer.id,
            writerNickname = recruitment.writer.nickname,
            groupId = recruitment.groupId,
            type = recruitment.type,
            title = recruitment.title,
            description = recruitment.description,
            purpose = recruitment.purpose,
            recruitmentStatus = recruitment.recruitmentStatus,
            recruitmentStart = recruitment.recruitmentStart,
            recruitmentEnd = recruitment.recruitmentEnd,
            targetMember = recruitment.targetMember,
            studyFormat = recruitment.studyFormat,
            contact = recruitment.contact,
            tags = recruitment.tags,
            createdAt = recruitment.createdAt,
            updatedAt = recruitment.updatedAt,
            isDeleted = recruitment.isDeleted,
            bumpAt = recruitment.bumpAt,
            bumpCount = recruitment.bumpCount
        )
    }
}
