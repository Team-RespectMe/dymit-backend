package net.noti_me.dymit.dymit_backend_api.units.controllers.study_schedule

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileThumbnailDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.StudyScheduleAttachmentService
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleAttachmentDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.StudyScheduleAttachmentController
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleAttachmentReplaceRequest
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class StudyScheduleAttachmentControllerTest : BehaviorSpec() {

    private val attachmentService = mockk<StudyScheduleAttachmentService>()

    private val controller = StudyScheduleAttachmentController(attachmentService)

    private val memberInfo = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "tester",
        roles = listOf(MemberRole.ROLE_MEMBER)
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("첨부 일괄 교체 요청이 주어지면") {
            When("컨트롤러의 replaceAttachments를 호출하면") {
                Then("요청을 커맨드로 전달하고 서비스 DTO를 응답 DTO로 변환한다") {
                    val scheduleId = ObjectId.get().toHexString()
                    val fileIds = listOf(ObjectId.get().toHexString(), ObjectId.get().toHexString())
                    val request = StudyScheduleAttachmentReplaceRequest(fileIds = fileIds)
                    val attachedAt = LocalDateTime.of(2026, 5, 1, 10, 0, 0)
                    val attachmentDto = createAttachmentDto(
                        fileId = fileIds[0],
                        attachedAt = attachedAt
                    )

                    every {
                        attachmentService.replaceAttachments(
                            memberInfo = memberInfo,
                            command = match { it.scheduleId == scheduleId && it.fileIds == fileIds }
                        )
                    } returns listOf(attachmentDto)

                    val response = controller.replaceAttachments(memberInfo, scheduleId, request)

                    verify(exactly = 1) {
                        attachmentService.replaceAttachments(
                            memberInfo = memberInfo,
                            command = match { it.scheduleId == scheduleId && it.fileIds == fileIds }
                        )
                    }
                    response.count shouldBe 1L
                    response.items[0].fileId shouldBe attachmentDto.fileId
                    response.items[0].url shouldBe attachmentDto.url
                    response.items[0].thumbnail?.url shouldBe attachmentDto.thumbnail?.url
                    response.items[0].contentType shouldBe attachmentDto.contentType
                    response.items[0].fileSize shouldBe attachmentDto.fileSize
                    response.items[0].attachedAt shouldBe attachedAt
                }
            }
        }

        Given("첨부 목록 조회 요청이 주어지면") {
            When("컨트롤러의 getAttachments를 호출하면") {
                Then("서비스 결과를 첨부 응답 목록으로 매핑한다") {
                    val scheduleId = ObjectId.get().toHexString()
                    val attachmentDto = createAttachmentDto(fileId = ObjectId.get().toHexString())

                    every {
                        attachmentService.getAttachments(
                            memberInfo = memberInfo,
                            scheduleId = scheduleId
                        )
                    } returns listOf(attachmentDto)

                    val response = controller.getAttachments(memberInfo, scheduleId)

                    verify(exactly = 1) {
                        attachmentService.getAttachments(
                            memberInfo = memberInfo,
                            scheduleId = scheduleId
                        )
                    }
                    response.count shouldBe 1L
                    response.items[0].fileId shouldBe attachmentDto.fileId
                    response.items[0].path shouldBe attachmentDto.path
                    response.items[0].status shouldBe attachmentDto.status
                }
            }
        }
    }

    private fun createAttachmentDto(
        fileId: String,
        attachedAt: LocalDateTime = LocalDateTime.of(2026, 5, 2, 11, 0, 0)
    ): StudyScheduleAttachmentDto {
        return StudyScheduleAttachmentDto(
            fileId = fileId,
            originalFileName = "file.pdf",
            contentType = "application/pdf",
            fileSize = 2048L,
            path = "/dymit/A/B/file.pdf",
            url = "https://cdn.example.com/dymit/A/B/file.pdf",
            thumbnail = FileThumbnailDto(
                path = "/dymit/thumbnails/A/B/file_thumbnail.jpg",
                url = "https://cdn.example.com/dymit/thumbnails/A/B/file_thumbnail.jpg"
            ),
            status = UserFileStatus.LINKED,
            attachedAt = attachedAt
        )
    }
}
