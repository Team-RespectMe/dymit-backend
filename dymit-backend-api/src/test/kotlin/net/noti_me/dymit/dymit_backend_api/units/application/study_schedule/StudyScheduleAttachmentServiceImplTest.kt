package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.file.FileServiceFacade
import net.noti_me.dymit.dymit_backend_api.application.file.FileUrlResolver
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ReplaceStudyScheduleAttachmentsCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.impl.StudyScheduleAttachmentServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFile
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleAttachment
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleAttachmentLinkQueryRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleAttachmentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class StudyScheduleAttachmentServiceImplTest : BehaviorSpec() {

    private val studyScheduleRepository = mockk<StudyScheduleRepository>()

    private val groupMemberRepository = mockk<StudyGroupMemberRepository>()

    private val scheduleAttachmentRepository = mockk<ScheduleAttachmentRepository>(
        moreInterfaces = arrayOf(ScheduleAttachmentLinkQueryRepository::class)
    )

    private val scheduleAttachmentLinkQueryRepository =
        scheduleAttachmentRepository as ScheduleAttachmentLinkQueryRepository

    private val userFileRepository = mockk<UserFileRepository>()

    private val fileServiceFacade = mockk<FileServiceFacade>()

    private val fileUrlResolver = mockk<FileUrlResolver>()

    private val service = StudyScheduleAttachmentServiceImpl(
        studyScheduleRepository = studyScheduleRepository,
        groupMemberRepository = groupMemberRepository,
        scheduleAttachmentRepository = scheduleAttachmentRepository,
        userFileRepository = userFileRepository,
        fileServiceFacade = fileServiceFacade,
        fileUrlResolver = fileUrlResolver
    )

    private val scheduleId = ObjectId.get()

    private val groupId = ObjectId.get()

    private val memberId = ObjectId.get()

    private val memberInfo = MemberInfo(
        memberId = memberId.toHexString(),
        nickname = "tester",
        roles = listOf(MemberRole.ROLE_MEMBER)
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
        scheduleAt = LocalDateTime.now().plusDays(1)
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
                    every { userFileRepository.findByIds(any()) } returns listOf(
                        createUserFile(id = existingFileId, status = UserFileStatus.LINKED),
                        createUserFile(id = newFileId, status = UserFileStatus.UPLOADED)
                    )
                    every {
                        scheduleAttachmentRepository.replaceByScheduleId(scheduleId = scheduleId, attachments = any())
                    } answers { secondArg() }
                    every { fileServiceFacade.updateFileStatus(any()) } returns createFileDto(newFileId, UserFileStatus.LINKED)

                    val result = service.replaceAttachments(
                        memberInfo = memberInfo,
                        command = ReplaceStudyScheduleAttachmentsCommand(
                            scheduleId = scheduleId.toHexString(),
                            fileIds = requestedFileIds
                        )
                    )

                    verify(exactly = 1) {
                        fileServiceFacade.updateFileStatus(match {
                            it.fileId == newFileId.toHexString() && it.status == UserFileStatus.LINKED
                        })
                    }
                    verify(exactly = 0) {
                        fileServiceFacade.updateFileStatus(match { it.status == UserFileStatus.UPLOADED })
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
                Then("제거된 파일 상태를 UPLOADED로 되돌린다") {
                    val removedFileId = ObjectId.get()
                    val remainedFileId = ObjectId.get()

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns listOf(
                        ScheduleAttachment(scheduleId = scheduleId, fileId = removedFileId),
                        ScheduleAttachment(scheduleId = scheduleId, fileId = remainedFileId)
                    )
                    every { userFileRepository.findByIds(any()) } returns listOf(
                        createUserFile(id = remainedFileId, status = UserFileStatus.LINKED)
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
                    every { fileServiceFacade.updateFileStatus(any()) } returns createFileDto(removedFileId, UserFileStatus.UPLOADED)

                    service.replaceAttachments(
                        memberInfo = memberInfo,
                        command = ReplaceStudyScheduleAttachmentsCommand(
                            scheduleId = scheduleId.toHexString(),
                            fileIds = listOf(remainedFileId.toHexString())
                        )
                    )

                    verify(exactly = 1) {
                        fileServiceFacade.updateFileStatus(match {
                            it.fileId == removedFileId.toHexString() && it.status == UserFileStatus.UPLOADED
                        })
                    }
                    verify(exactly = 0) {
                        fileServiceFacade.updateFileStatus(match {
                            it.fileId == remainedFileId.toHexString() && it.status == UserFileStatus.LINKED
                        })
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
                Then("해당 파일은 UPLOADED로 강등하지 않는다") {
                    val removedButStillLinkedFileId = ObjectId.get()
                    val remainedFileId = ObjectId.get()

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns listOf(
                        ScheduleAttachment(scheduleId = scheduleId, fileId = removedButStillLinkedFileId),
                        ScheduleAttachment(scheduleId = scheduleId, fileId = remainedFileId)
                    )
                    every { userFileRepository.findByIds(any()) } returns listOf(
                        createUserFile(id = remainedFileId, status = UserFileStatus.LINKED)
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
                        fileServiceFacade.updateFileStatus(match {
                            it.fileId == removedButStillLinkedFileId.toHexString() && it.status == UserFileStatus.UPLOADED
                        })
                    }
                }
            }
        }

        Given("빈 fileIds로 전체 해제 요청이 주어지면") {
            When("replaceAttachments를 호출하면") {
                Then("모든 기존 파일을 UPLOADED로 되돌리고 첨부를 비운다") {
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
                    every { fileServiceFacade.updateFileStatus(any()) } returns createFileDto(firstFileId, UserFileStatus.UPLOADED)

                    val result = service.replaceAttachments(
                        memberInfo = memberInfo,
                        command = ReplaceStudyScheduleAttachmentsCommand(
                            scheduleId = scheduleId.toHexString(),
                            fileIds = emptyList()
                        )
                    )

                    verify(exactly = 1) {
                        fileServiceFacade.updateFileStatus(match {
                            it.fileId == firstFileId.toHexString() && it.status == UserFileStatus.UPLOADED
                        })
                    }
                    verify(exactly = 1) {
                        fileServiceFacade.updateFileStatus(match {
                            it.fileId == secondFileId.toHexString() && it.status == UserFileStatus.UPLOADED
                        })
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
                    verify(exactly = 0) { userFileRepository.findByIds(any()) }
                    result shouldBe emptyList()
                }
            }
        }

        Given("첨부 조회 대상 파일에 썸네일과 메타데이터가 존재하면") {
            When("getAttachments를 호출하면") {
                Then("url, thumbnail, contentType, fileSize가 포함된 DTO를 반환한다") {
                    val fileId = ObjectId.get()
                    val attachedAt = LocalDateTime.of(2026, 5, 1, 10, 30, 0)

                    stubMemberValidation(memberExists = true)
                    every { scheduleAttachmentRepository.findByScheduleId(scheduleId) } returns listOf(
                        ScheduleAttachment(
                            scheduleId = scheduleId,
                            fileId = fileId,
                            createdAt = attachedAt
                        )
                    )
                    every { userFileRepository.findByIds(listOf(fileId)) } returns listOf(
                        createUserFile(
                            id = fileId,
                            status = UserFileStatus.LINKED,
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
                    every { userFileRepository.findByIds(listOf(fileId)) } returns listOf(
                        createUserFile(id = fileId, status = UserFileStatus.REQUESTED)
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
                    every { userFileRepository.findByIds(listOf(fileId)) } returns listOf(
                        createUserFile(id = fileId, status = UserFileStatus.FAILED)
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
                    verify(exactly = 0) { fileServiceFacade.updateFileStatus(any()) }
                    verify(exactly = 0) {
                        scheduleAttachmentRepository.replaceByScheduleId(any(), any())
                    }
                }
            }
        }
    }

    private fun stubMemberValidation(memberExists: Boolean) {
        every { studyScheduleRepository.loadById(scheduleId) } returns schedule
        every {
            groupMemberRepository.findByGroupIdAndMemberId(groupId = groupId, memberId = memberId)
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
        every { fileUrlResolver.resolve(any()) } answers {
            "https://cdn.example.com${firstArg<String>()}"
        }
        every { fileUrlResolver.resolveOrNull(any()) } answers {
            firstArg<String?>()?.let { path -> "https://cdn.example.com$path" }
        }
    }

    private fun createUserFile(
        id: ObjectId,
        status: UserFileStatus,
        contentType: String = "application/pdf",
        fileSize: Long = 1024L,
        thumbnailPath: String? = null
    ): UserFile {
        return UserFile(
            id = id,
            memberId = memberId,
            originalFileName = "file.pdf",
            storedFileName = "FILE_2026_05_01_10_30_00.pdf",
            path = "/dymit/A/B/file.pdf",
            thumbnailPath = thumbnailPath,
            status = status,
            contentType = contentType,
            fileSize = fileSize
        )
    }

    private fun createFileDto(
        fileId: ObjectId,
        status: UserFileStatus
    ): FileDto {
        return FileDto(
            fileId = fileId.toHexString(),
            status = status,
            originalFileName = "file.pdf",
            path = "/dymit/A/B/file.pdf",
            url = "https://cdn.example.com/dymit/A/B/file.pdf"
        )
    }
}
