package net.noti_me.dymit.dymit_backend_api.units.file.application.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.UpdateFileStatusCommand
import net.noti_me.dymit.dymit_backend_api.file.application.UpdateFileStatusUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import net.noti_me.dymit.dymit_backend_api.file.domain.UserFile
import net.noti_me.dymit.dymit_backend_api.file.domain.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.FileStatusDto
import net.noti_me.dymit.dymit_backend_api.file.application.port.out.persistence.UserFileRepository
import org.bson.types.ObjectId

internal class UpdateFileStatusUseCaseImplTest : BehaviorSpec() {

    private val userFileRepository = mockk<UserFileRepository>()

    private val cdnConfig = mockk<CDNConfig>()

    private val useCase = UpdateFileStatusUseCaseImpl(
        userFileRepository = userFileRepository,
        cdnConfig = cdnConfig
    )

    init {
        beforeEach {
            every { cdnConfig.getDomain() } returns "https://cdn.example.com"
        }

        Given("업로드가 완료된 파일이 있으면") {
            val userFile = UserFile(
                id = ObjectId.get(),
                memberId = ObjectId.get(),
                originalFileName = "hello.txt",
                storedFileName = "FILE_2026_04_27_21_10_11.txt",
                path = "/dymit/A/B/FILE_2026_04_27_21_10_11.txt",
                status = UserFileStatus.UPLOADED,
                contentType = "text/plain",
                fileSize = 5L
            )
            every { userFileRepository.findById(userFile.identifier) } returns userFile
            every { userFileRepository.save(any()) } answers { firstArg() }

            When("LINKED 상태로 갱신하면") {
                val command = UpdateFileStatusCommand(
                    fileId = userFile.identifier,
                    status = FileStatusDto.LINKED
                )

                Then("상태를 갱신하고 CDN URL을 유지한 채 반환한다") {
                    val result = useCase.execute(command)

                    result.status shouldBe FileStatusDto.LINKED
                    result.originalFileName shouldBe "hello.txt"
                    result.url shouldBe "https://cdn.example.com${userFile.path}"
                }
            }
        }

        Given("이미 LINKED 상태인 파일이 있으면") {
            When("UPLOADED 상태로 갱신하면") {
                val userFile = UserFile(
                    id = ObjectId.get(),
                    memberId = ObjectId.get(),
                    originalFileName = "linked.pdf",
                    storedFileName = "FILE_2026_05_01_20_10_11.pdf",
                    path = "/dymit/A/B/FILE_2026_05_01_20_10_11.pdf",
                    status = UserFileStatus.LINKED,
                    contentType = "application/pdf",
                    fileSize = 512L
                )
                every { userFileRepository.findById(userFile.identifier) } returns userFile
                every { userFileRepository.save(any()) } answers { firstArg() }
                val command = UpdateFileStatusCommand(
                    fileId = userFile.identifier,
                    status = FileStatusDto.UPLOADED
                )

                Then("허용된 상태 전이로 처리된다") {
                    val result = useCase.execute(command)

                    result.status shouldBe FileStatusDto.UPLOADED
                    result.url shouldBe "https://cdn.example.com${userFile.path}"
                }
            }

            When("UNREFERENCED 상태로 갱신하면") {
                val userFile = UserFile(
                    id = ObjectId.get(),
                    memberId = ObjectId.get(),
                    originalFileName = "linked.pdf",
                    storedFileName = "FILE_2026_05_01_20_10_11.pdf",
                    path = "/dymit/A/B/FILE_2026_05_01_20_10_11.pdf",
                    status = UserFileStatus.LINKED,
                    contentType = "application/pdf",
                    fileSize = 512L
                )
                every { userFileRepository.findById(userFile.identifier) } returns userFile
                every { userFileRepository.save(any()) } answers { firstArg() }
                val command = UpdateFileStatusCommand(
                    fileId = userFile.identifier,
                    status = FileStatusDto.UNREFERENCED
                )

                Then("허용된 상태 전이로 처리된다") {
                    val result = useCase.execute(command)

                    result.status shouldBe FileStatusDto.UNREFERENCED
                    result.url shouldBe "https://cdn.example.com${userFile.path}"
                }
            }
        }

        Given("REQUESTED 상태 파일이 있으면") {
            val userFile = UserFile(
                id = ObjectId.get(),
                memberId = ObjectId.get(),
                originalFileName = "hello.txt",
                storedFileName = "FILE_2026_04_27_21_10_11.txt",
                path = "/dymit/A/B/FILE_2026_04_27_21_10_11.txt",
                status = UserFileStatus.REQUESTED,
                contentType = "text/plain",
                fileSize = 5L
            )
            every { userFileRepository.findById(userFile.identifier) } returns userFile

            When("바로 LINKED 상태로 갱신하면") {
                val command = UpdateFileStatusCommand(
                    fileId = userFile.identifier,
                    status = FileStatusDto.LINKED
                )

                Then("허용되지 않은 상태 전이 예외가 발생한다") {
                    shouldThrow<BadRequestException> {
                        useCase.execute(command)
                    }
                }
            }
        }

        Given("UNREFERENCED 상태 파일이 있으면") {
            When("LINKED 상태로 갱신하면") {
                val userFile = UserFile(
                    id = ObjectId.get(),
                    memberId = ObjectId.get(),
                    originalFileName = "unreferenced.pdf",
                    storedFileName = "FILE_2026_05_02_21_10_11.pdf",
                    path = "/dymit/A/B/FILE_2026_05_02_21_10_11.pdf",
                    status = UserFileStatus.UNREFERENCED,
                    contentType = "application/pdf",
                    fileSize = 256L
                )
                every { userFileRepository.findById(userFile.identifier) } returns userFile
                every { userFileRepository.save(any()) } answers { firstArg() }
                val command = UpdateFileStatusCommand(
                    fileId = userFile.identifier,
                    status = FileStatusDto.LINKED
                )

                Then("허용된 상태 전이로 처리된다") {
                    val result = useCase.execute(command)

                    result.status shouldBe FileStatusDto.LINKED
                }
            }

            When("UPLOADED 상태로 갱신하면") {
                val userFile = UserFile(
                    id = ObjectId.get(),
                    memberId = ObjectId.get(),
                    originalFileName = "unreferenced.pdf",
                    storedFileName = "FILE_2026_05_02_21_10_11.pdf",
                    path = "/dymit/A/B/FILE_2026_05_02_21_10_11.pdf",
                    status = UserFileStatus.UNREFERENCED,
                    contentType = "application/pdf",
                    fileSize = 256L
                )
                every { userFileRepository.findById(userFile.identifier) } returns userFile
                every { userFileRepository.save(any()) } answers { firstArg() }
                val command = UpdateFileStatusCommand(
                    fileId = userFile.identifier,
                    status = FileStatusDto.UPLOADED
                )

                Then("허용되지 않은 상태 전이 예외가 발생한다") {
                    shouldThrow<BadRequestException> {
                        useCase.execute(command)
                    }
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }
}
