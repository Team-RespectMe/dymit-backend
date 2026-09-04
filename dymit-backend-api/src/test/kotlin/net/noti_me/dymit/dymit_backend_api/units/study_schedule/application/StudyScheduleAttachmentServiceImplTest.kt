package net.noti_me.dymit.dymit_backend_api.units.study_schedule.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.ReplaceStudyScheduleAttachmentsCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.StudyScheduleAttachmentServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleAttachment
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.StudyScheduleGroupPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.StudyScheduleFilePort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto.StudyScheduleFileDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto.StudyScheduleFileStatusDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleAttachmentLinkQueryRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleAttachmentRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.StudyScheduleRepository
import org.bson.types.ObjectId
import java.time.Instant

internal class StudyScheduleAttachmentServiceImplTest : BehaviorSpec() {

    private val studyScheduleRepository = mockk<StudyScheduleRepository>()

    private val groupPort = mockk<StudyScheduleGroupPort>()

    private val scheduleAttachmentRepository = mockk<ScheduleAttachmentRepository>(
        moreInterfaces = arrayOf(ScheduleAttachmentLinkQueryRepository::class)
    )

    private val scheduleAttachmentLinkQueryRepository =
        scheduleAttachmentRepository as ScheduleAttachmentLinkQueryRepository

    private val filePort = mockk<StudyScheduleFilePort>()

    private val service = StudyScheduleAttachmentServiceImpl(
        studyScheduleRepository = studyScheduleRepository,
        groupPort = groupPort,
        scheduleAttachmentRepository = scheduleAttachmentRepository,
        filePort = filePort
    )

    private val scheduleId = ObjectId.get()

    private val groupId = ObjectId.get()

    private val memberId = ObjectId.get()

    private val memberInfo = MemberInfo(
        memberId = memberId.toHexString(),
        nickname = "tester",
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )

    private val schedule = StudySchedule(
        id = scheduleId,
        groupId = groupId,
        title = "자료 공유 일정",
        description = "첨부 테스트",
        location = ScheduleLocation(
            type = ScheduleLocation.LocationType.ONLINE,
            value = "Zoom",
            link = "https://zoom.us/j/123"
        ),
        scheduleAt = Instant.now().plusSeconds(1L * 86400L)
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("기존 첨부 1개에 신규 업로드 파일 1개를 추가하는 교체 요청이 주어지면") {
            When("replaceAttachments를 호출하면") {
                Then("신규 파일만 LINKED로 변경하고 최종 첨부 목록을 반환한다") {
                    val existingFileId = ObjectId.get()
                    val newFileId = ObjectId.get()
                    val requestedFileIds = listOf(existingFileId.toHexString(), newFileId.toHexString())

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns listOf(
                        ScheduleAttachment(
                            scheduleId = scheduleId,
                            fileId = existingFileId
                        )
                    )
                    every { filePort.loadByIds(any()) } returns listOf(
                        createUserFile(id = existingFileId, status = StudyScheduleFileStatusDto.LINKED),
                        createUserFile(id = newFileId, status = StudyScheduleFileStatusDto.UPLOADED)
                    )
                    every {
                        scheduleAttachmentRepository.replaceByScheduleId(scheduleId = scheduleId, attachments = any())
                    } answers { secondArg() }
                    every { filePort.updateStatus(any(), any()) } returns StudyScheduleFileStatusDto.LINKED

                    val result = service.replaceAttachments(
                        memberInfo = memberInfo,
                        command = ReplaceStudyScheduleAttachmentsCommand(
                            scheduleId = scheduleId.toHexString(),
                            fileIds = requestedFileIds
                        )
                    )

                    verify(exactly = 1) {
                        filePort.updateStatus(newFileId, StudyScheduleFileStatusDto.LINKED)
                    }
                    verify(exactly = 0) {
                        filePort.updateStatus(any(), StudyScheduleFileStatusDto.UPLOADED)
                    }
                    verify(exactly = 1) {
                        scheduleAttachmentRepository.replaceByScheduleId(
                            scheduleId = scheduleId,
                            attachments = match { it.map { attachment -> attachment.fileId } == listOf(existingFileId, newFileId) }
                        )
                    }
                    result.map { it.fileId } shouldContainExactly requestedFileIds
                }
            }
        }

        Given("기존 첨부 2개 중 1개를 제거하는 교체 요청이 주어지면") {
            When("replaceAttachments를 호출하면") {
                Then("제거된 파일 상태를 UNREFERENCED로 변경한다") {
                    val removedFileId = ObjectId.get()
                    val remainedFileId = ObjectId.get()

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns listOf(
                        ScheduleAttachment(scheduleId = scheduleId, fileId = removedFileId),
                        ScheduleAttachment(scheduleId = scheduleId, fileId = remainedFileId)
                    )
                    every { filePort.loadByIds(any()) } returns listOf(
                        createUserFile(id = remainedFileId, status = StudyScheduleFileStatusDto.LINKED)
                    )
                    every {
                        scheduleAttachmentRepository.replaceByScheduleId(scheduleId = scheduleId, attachments = any())
                    } answers { secondArg() }
                    every {
                        scheduleAttachmentLinkQueryRepository.findAttachedFileIdsExcludingSchedule(
                            fileIds = listOf(removedFileId),
                            scheduleId = scheduleId
                        )
                    } returns emptySet()
                    every { filePort.updateStatus(any(), any()) } returns StudyScheduleFileStatusDto.UNREFERENCED

                    service.replaceAttachments(
                        memberInfo = memberInfo,
                        command = ReplaceStudyScheduleAttachmentsCommand(
                            scheduleId = scheduleId.toHexString(),
                            fileIds = listOf(remainedFileId.toHexString())
                        )
                    )

                    verify(exactly = 1) {
                        filePort.updateStatus(removedFileId, StudyScheduleFileStatusDto.UNREFERENCED)
                    }
                    verify(exactly = 0) {
                        filePort.updateStatus(remainedFileId, StudyScheduleFileStatusDto.LINKED)
                    }
                    verify(exactly = 1) {
                        scheduleAttachmentLinkQueryRepository.findAttachedFileIdsExcludingSchedule(
                            fileIds = listOf(removedFileId),
                            scheduleId = scheduleId
                        )
                    }
                }
            }
        }

        Given("현재 일정에서 제거되지만 다른 일정에 여전히 첨부된 파일이 있으면") {
            When("replaceAttachments를 호출하면") {
                Then("해당 파일은 UNREFERENCED로 강등하지 않는다") {
                    val removedButStillLinkedFileId = ObjectId.get()
                    val remainedFileId = ObjectId.get()

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns listOf(
                        ScheduleAttachment(scheduleId = scheduleId, fileId = removedButStillLinkedFileId),
                        ScheduleAttachment(scheduleId = scheduleId, fileId = remainedFileId)
                    )
                    every { filePort.loadByIds(any()) } returns listOf(
                        createUserFile(id = remainedFileId, status = StudyScheduleFileStatusDto.LINKED)
                    )
                    every {
                        scheduleAttachmentRepository.replaceByScheduleId(scheduleId = scheduleId, attachments = any())
                    } answers { secondArg() }
                    every {
                        scheduleAttachmentLinkQueryRepository.findAttachedFileIdsExcludingSchedule(
                            fileIds = listOf(removedButStillLinkedFileId),
                            scheduleId = scheduleId
                        )
                    } returns setOf(removedButStillLinkedFileId)

                    service.replaceAttachments(
                        memberInfo = memberInfo,
                        command = ReplaceStudyScheduleAttachmentsCommand(
                            scheduleId = scheduleId.toHexString(),
                            fileIds = listOf(remainedFileId.toHexString())
                        )
                    )

                    verify(exactly = 1) {
                        scheduleAttachmentLinkQueryRepository.findAttachedFileIdsExcludingSchedule(
                            fileIds = listOf(removedButStillLinkedFileId),
                            scheduleId = scheduleId
                        )
                    }
                    verify(exactly = 0) {
                        filePort.updateStatus(
                            removedButStillLinkedFileId,
                            StudyScheduleFileStatusDto.UNREFERENCED
                        )
                    }
                }
            }
        }

        Given("빈 fileIds로 전체 해제 요청이 주어지면") {
            When("replaceAttachments를 호출하면") {
                Then("모든 기존 파일을 UNREFERENCED로 변경하고 첨부를 비운다") {
                    val firstFileId = ObjectId.get()
                    val secondFileId = ObjectId.get()

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns listOf(
                        ScheduleAttachment(scheduleId = scheduleId, fileId = firstFileId),
                        ScheduleAttachment(scheduleId = scheduleId, fileId = secondFileId)
                    )
                    every {
                        scheduleAttachmentRepository.replaceByScheduleId(scheduleId = scheduleId, attachments = any())
                    } returns emptyList()
                    every {
                        scheduleAttachmentLinkQueryRepository.findAttachedFileIdsExcludingSchedule(
                            fileIds = match { it.toSet() == setOf(firstFileId, secondFileId) },
                            scheduleId = scheduleId
                        )
                    } returns emptySet()
                    every { filePort.updateStatus(any(), any()) } returns StudyScheduleFileStatusDto.UNREFERENCED

                    val result = service.replaceAttachments(
                        memberInfo = memberInfo,
                        command = ReplaceStudyScheduleAttachmentsCommand(
                            scheduleId = scheduleId.toHexString(),
                            fileIds = emptyList()
                        )
                    )

                    verify(exactly = 1) {
                        filePort.updateStatus(firstFileId, StudyScheduleFileStatusDto.UNREFERENCED)
                    }
                    verify(exactly = 1) {
                        filePort.updateStatus(secondFileId, StudyScheduleFileStatusDto.UNREFERENCED)
                    }
                    verify(exactly = 1) {
                        scheduleAttachmentRepository.replaceByScheduleId(
                            scheduleId = scheduleId,
                            attachments = match { it.isEmpty() }
                        )
                    }
                    verify(exactly = 1) {
                        scheduleAttachmentLinkQueryRepository.findAttachedFileIdsExcludingSchedule(
                            fileIds = match { it.toSet() == setOf(firstFileId, secondFileId) },
                            scheduleId = scheduleId
                        )
                    }
                    verify(exactly = 0) { filePort.loadByIds(any()) }
                    result shouldBe emptyList()
                }
            }
        }

        Given("첨부 조회 대상 파일에 썸네일과 메타데이터가 존재하면") {
            When("getAttachments를 호출하면") {
                Then("url, thumbnail, contentType, fileSize가 포함된 DTO를 반환한다") {
                    val fileId = ObjectId.get()
                    val attachedAt = Instant.parse("2026-05-01T10:30:00Z")

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns listOf(
                        ScheduleAttachment(
                            scheduleId = scheduleId,
                            fileId = fileId,
                            createdAt = attachedAt
                        )
                    )
                    every { filePort.loadByIds(listOf(fileId)) } returns listOf(
                        createUserFile(
                            id = fileId,
                            status = StudyScheduleFileStatusDto.LINKED,
                            contentType = "application/pdf",
                            fileSize = 4096L,
                            thumbnailPath = "/dymit/thumbnails/A/B/file_thumbnail.jpg"
                        )
                    )

                    val result = service.getAttachments(
                        memberInfo = memberInfo,
                        scheduleId = scheduleId.toHexString()
                    )

                    result.size shouldBe 1
                    result[0].url shouldBe "https://cdn.example.com/dymit/A/B/file.pdf"
                    result[0].thumbnail?.url shouldBe "https://cdn.example.com/dymit/thumbnails/A/B/file_thumbnail.jpg"
                    result[0].contentType shouldBe "application/pdf"
                    result[0].fileSize shouldBe 4096L
                }
            }
        }

        Given("그룹 멤버가 아닌 요청자가 첨부 교체를 시도하면") {
            When("replaceAttachments를 호출하면") {
                Then("ForbiddenException이 발생한다") {
                    stubMemberValidation(memberExists = false)

                    shouldThrow<ForbiddenException> {
                        service.replaceAttachments(
                            memberInfo = memberInfo,
                            command = ReplaceStudyScheduleAttachmentsCommand(
                                scheduleId = scheduleId.toHexString(),
                                fileIds = emptyList()
                            )
                        )
                    }
                }
            }
        }

        Given("REQUESTED 상태 파일을 첨부하려고 하면") {
            When("replaceAttachments를 호출하면") {
                Then("BadRequestException이 발생한다") {
                    val fileId = ObjectId.get()

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns emptyList()
                    every { filePort.loadByIds(listOf(fileId)) } returns listOf(
                        createUserFile(id = fileId, status = StudyScheduleFileStatusDto.REQUESTED)
                    )

                    shouldThrow<BadRequestException> {
                        service.replaceAttachments(
                            memberInfo = memberInfo,
                            command = ReplaceStudyScheduleAttachmentsCommand(
                                scheduleId = scheduleId.toHexString(),
                                fileIds = listOf(fileId.toHexString())
                            )
                        )
                    }
                }
            }
        }

        Given("FAILED 상태 파일을 첨부하려고 하면") {
            When("replaceAttachments를 호출하면") {
                Then("BadRequestException이 발생한다") {
                    val fileId = ObjectId.get()

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns emptyList()
                    every { filePort.loadByIds(listOf(fileId)) } returns listOf(
                        createUserFile(id = fileId, status = StudyScheduleFileStatusDto.FAILED)
                    )

                    shouldThrow<BadRequestException> {
                        service.replaceAttachments(
                            memberInfo = memberInfo,
                            command = ReplaceStudyScheduleAttachmentsCommand(
                                scheduleId = scheduleId.toHexString(),
                                fileIds = listOf(fileId.toHexString())
                            )
                        )
                    }
                    verify(exactly = 0) { filePort.updateStatus(any(), any()) }
                    verify(exactly = 0) {
                        scheduleAttachmentRepository.replaceByScheduleId(any(), any())
                    }
                }
            }
        }

        Given("UNREFERENCED 상태 파일을 첨부하려고 하면") {
            When("replaceAttachments를 호출하면") {
                Then("첨부 가능하며 LINKED로 승격된다") {
                    val fileId = ObjectId.get()

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns emptyList()
                    every { filePort.loadByIds(listOf(fileId)) } returnsMany listOf(
                        listOf(createUserFile(id = fileId, status = StudyScheduleFileStatusDto.UNREFERENCED)),
                        listOf(createUserFile(id = fileId, status = StudyScheduleFileStatusDto.LINKED))
                    )
                    every {
                        scheduleAttachmentRepository.replaceByScheduleId(scheduleId = scheduleId, attachments = any())
                    } answers { secondArg() }
                    every { filePort.updateStatus(any(), any()) } returns StudyScheduleFileStatusDto.LINKED

                    val result = service.replaceAttachments(
                        memberInfo = memberInfo,
                        command = ReplaceStudyScheduleAttachmentsCommand(
                            scheduleId = scheduleId.toHexString(),
                            fileIds = listOf(fileId.toHexString())
                        )
                    )

                    verify(exactly = 1) {
                        filePort.updateStatus(fileId, StudyScheduleFileStatusDto.LINKED)
                    }
                    result[0].status shouldBe StudyScheduleFileStatusDto.LINKED
                    result.map { it.fileId } shouldContainExactly listOf(fileId.toHexString())
                }
            }
        }
    }

    private fun stubMemberValidation(memberExists: Boolean) {
        every { studyScheduleRepository.loadById(scheduleId) } returns schedule
        every {
            groupPort.findMember(groupId = groupId, memberId = memberId)
        } returns if ( memberExists ) {
            StudyGroupMember(
                id = ObjectId.get(),
                groupId = groupId,
                memberId = memberId,
                nickname = "tester"
            )
        } else {
            null
        }
    }

    private fun createUserFile(
        id: ObjectId,
        status: StudyScheduleFileStatusDto,
        contentType: String = "application/pdf",
        fileSize: Long = 1024L,
        thumbnailPath: String? = null
    ): StudyScheduleFileDto {
        val path = "/dymit/A/B/file.pdf"
        return StudyScheduleFileDto(
            id = id,
            originalFileName = "file.pdf",
            path = path,
            thumbnailPath = thumbnailPath,
            status = status,
            contentType = contentType,
            fileSize = fileSize,
            url = "https://cdn.example.com$path",
            thumbnailUrl = thumbnailPath?.let { "https://cdn.example.com$it" }
        )
    }
}
