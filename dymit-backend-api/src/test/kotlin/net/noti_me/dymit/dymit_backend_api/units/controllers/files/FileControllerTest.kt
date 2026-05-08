package net.noti_me.dymit.dymit_backend_api.units.controllers.files

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.file.FileServiceFacade
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileThumbnailDto
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.files.FileController
import net.noti_me.dymit.dymit_backend_api.controllers.files.dto.FileUploadRequest
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import org.bson.types.ObjectId
import org.springframework.mock.web.MockMultipartFile

internal class FileControllerTest : BehaviorSpec() {

    private val fileServiceFacade = mockk<FileServiceFacade>()

    private val controller = FileController(fileServiceFacade)

    private val loginMember = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "member",
        roles = listOf(MemberRole.ROLE_MEMBER)
    )

    init {
        beforeTest {
            clearAllMocks()
        }

        Given("썸네일이 포함된 파일 업로드 응답 DTO가 주어지면") {
            When("컨트롤러에서 업로드를 처리하면") {
                Then("서비스 파사드에 변환된 커맨드를 전달하고 썸네일 필드까지 응답으로 매핑한다") {
                    val multipartFile = MockMultipartFile(
                        "file",
                        "hello.jpg",
                        "image/jpeg",
                        "hello".toByteArray()
                    )
                    val request = FileUploadRequest(file = multipartFile)
                    val fileDto = FileDto(
                        fileId = ObjectId.get().toHexString(),
                        status = UserFileStatus.UPLOADED,
                        originalFileName = "hello.jpg",
                        path = "/dymit/A/B/ABCDEF_2026_04_27_21_10_11.jpg",
                        url = "https://cdn.example.com/dymit/A/B/ABCDEF_2026_04_27_21_10_11.jpg",
                        thumbnail = FileThumbnailDto(
                            path = "/dymit/thumbnails/A/B/ABCDEF_2026_04_27_21_10_11_thumbnail.jpg",
                            url = "https://cdn.example.com/dymit/thumbnails/A/B/ABCDEF_2026_04_27_21_10_11_thumbnail.jpg"
                        )
                    )

                    every {
                        fileServiceFacade.uploadFile(
                            loginMember = loginMember,
                            command = match {
                                it.file == multipartFile && it.enforceFileApiPolicy
                            }
                        )
                    } returns fileDto

                    val response = controller.uploadFile(loginMember, request)

                    verify(exactly = 1) {
                        fileServiceFacade.uploadFile(
                            loginMember = loginMember,
                            command = match {
                                it.file == multipartFile && it.enforceFileApiPolicy
                            }
                        )
                    }
                    response.fileId shouldBe fileDto.fileId
                    response.status shouldBe UserFileStatus.UPLOADED
                    response.originalFileName shouldBe fileDto.originalFileName
                    response.path shouldBe fileDto.path
                    response.url shouldBe fileDto.url
                    response.thumbnail?.path shouldBe fileDto.thumbnail?.path
                    response.thumbnail?.url shouldBe fileDto.thumbnail?.url
                }
            }
        }

        Given("썸네일이 없는 파일 업로드 응답 DTO가 주어지면") {
            When("컨트롤러에서 업로드를 처리하면") {
                Then("썸네일 필드를 null 로 유지한 채 응답으로 매핑한다") {
                    val multipartFile = MockMultipartFile(
                        "file",
                        "hello.pdf",
                        "application/pdf",
                        "hello".toByteArray()
                    )
                    val request = FileUploadRequest(file = multipartFile)
                    val fileDto = FileDto(
                        fileId = ObjectId.get().toHexString(),
                        status = UserFileStatus.UPLOADED,
                        originalFileName = "hello.pdf",
                        path = "/dymit/A/B/ABCDEF_2026_04_27_21_10_11.pdf",
                        url = "https://cdn.example.com/dymit/A/B/ABCDEF_2026_04_27_21_10_11.pdf"
                    )

                    every {
                        fileServiceFacade.uploadFile(
                            loginMember = loginMember,
                            command = match {
                                it.file == multipartFile && it.enforceFileApiPolicy
                            }
                        )
                    } returns fileDto

                    val response = controller.uploadFile(loginMember, request)

                    verify(exactly = 1) {
                        fileServiceFacade.uploadFile(
                            loginMember = loginMember,
                            command = match {
                                it.file == multipartFile && it.enforceFileApiPolicy
                            }
                        )
                    }
                    response.fileId shouldBe fileDto.fileId
                    response.status shouldBe UserFileStatus.UPLOADED
                    response.originalFileName shouldBe fileDto.originalFileName
                    response.path shouldBe fileDto.path
                    response.url shouldBe fileDto.url
                    response.thumbnail.shouldBeNull()
                }
            }
        }
    }
}
