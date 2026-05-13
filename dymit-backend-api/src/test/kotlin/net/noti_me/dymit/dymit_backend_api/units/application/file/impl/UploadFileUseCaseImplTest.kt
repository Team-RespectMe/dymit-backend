package net.noti_me.dymit.dymit_backend_api.units.application.file.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadResult
import net.noti_me.dymit.dymit_backend_api.application.file.impl.S3FileService
import net.noti_me.dymit.dymit_backend_api.application.file.impl.UploadFileUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.InternalServerError
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFile
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import org.bson.types.ObjectId
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal class UploadFileUseCaseImplTest : BehaviorSpec() {

    private val userFileRepository = mockk<UserFileRepository>()

    private val s3FileService = mockk<S3FileService>()

    private val cdnConfig = mockk<CDNConfig>()

    private val useCase = UploadFileUseCaseImpl(
        userFileRepository = userFileRepository,
        s3FileService = s3FileService,
        cdnConfig = cdnConfig
    )

    private val loginMember = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "member",
        roles = listOf(MemberRole.ROLE_MEMBER)
    )

    init {
        beforeTest {
            clearAllMocks()
            every { cdnConfig.getDomain() } returns "https://cdn.example.com"
        }

        Given("JPEG 파일 업로드 요청이 주어지면") {
            When("원본 파일과 썸네일 업로드가 모두 성공하면") {
                Then("썸네일 메타데이터를 저장하고 응답에 포함한다") {
                    val multipartFile = createJpegMultipartFile()
                    val command = FileUploadCommand(file = multipartFile).enforceFileApiPolicy()
                    val savedStatuses = mutableListOf<UserFileStatus>()
                    val savedFiles = mutableListOf<UserFile>()
                    val uploadedPaths = mutableListOf<String>()
                    var uploadedThumbnailSize: Pair<Int, Int>? = null

                    stubRepositorySave(savedStatuses, savedFiles)
                    every { s3FileService.upload(any(), any()) } answers {
                        val uploadedFile = firstArg<MultipartFile>()
                        val path = secondArg<String>()
                        uploadedPaths.add(path)
                        if ( path.startsWith("/dymit/thumbnails/") ) {
                            uploadedThumbnailSize = readImageSize(uploadedFile.bytes)
                        }
                        FileUploadResult(
                            path = path,
                            accessUrl = "https://origin.example.com$path"
                        )
                    }

                    val result = useCase.uploadFile(loginMember, command)

                    result.status shouldBe UserFileStatus.UPLOADED
                    result.originalFileName shouldBe "thumbnail-source.jpg"
                    result.url shouldBe "https://cdn.example.com${result.path}"
                    result.thumbnail?.path shouldStartWith "/dymit/thumbnails/"
                    result.thumbnail?.url shouldBe "https://cdn.example.com${result.thumbnail!!.path}"
                    uploadedPaths.size shouldBe 2
                    uploadedPaths[0] shouldStartWith "/dymit/"
                    uploadedPaths[1] shouldStartWith "/dymit/thumbnails/"
                    uploadedThumbnailSize shouldBe Pair(320, 240)
                    savedStatuses.shouldContainExactly(
                        UserFileStatus.REQUESTED,
                        UserFileStatus.UPLOADED
                    )
                    savedFiles.first().thumbnailPath shouldStartWith "/dymit/thumbnails/"
                    verify(exactly = 2) { s3FileService.upload(any(), any()) }
                }
            }

            When("썸네일 업로드가 실패하면") {
                Then("파일 상태를 FAILED 로 갱신하고 예외를 던진다") {
                    val multipartFile = createJpegMultipartFile()
                    val command = FileUploadCommand(file = multipartFile).enforceFileApiPolicy()
                    val savedStatuses = mutableListOf<UserFileStatus>()
                    var uploadCount = 0

                    stubRepositorySave(savedStatuses)
                    every { s3FileService.upload(any(), any()) } answers {
                        uploadCount += 1
                        if ( uploadCount == 2 ) {
                            throw RuntimeException("thumbnail failure")
                        }
                        val path = secondArg<String>()
                        FileUploadResult(
                            path = path,
                            accessUrl = "https://origin.example.com$path"
                        )
                    }

                    shouldThrow<InternalServerError> {
                        useCase.uploadFile(loginMember, command)
                    }
                    savedStatuses.shouldContainExactly(
                        UserFileStatus.REQUESTED,
                        UserFileStatus.FAILED
                    )
                }
            }
        }

        Given("PDF 파일 업로드 요청이 주어지면") {
            When("S3 업로드에 성공하면") {
                Then("썸네일 없이 업로드를 완료한다") {
                    val multipartFile = createPdfMultipartFile()
                    val command = FileUploadCommand(file = multipartFile).enforceFileApiPolicy()
                    val savedStatuses = mutableListOf<UserFileStatus>()
                    val savedFiles = mutableListOf<UserFile>()

                    stubRepositorySave(savedStatuses, savedFiles)
                    every { s3FileService.upload(any(), any()) } answers {
                        val path = secondArg<String>()
                        FileUploadResult(
                            path = path,
                            accessUrl = "https://origin.example.com$path"
                        )
                    }

                    val result = useCase.uploadFile(loginMember, command)

                    result.status shouldBe UserFileStatus.UPLOADED
                    result.originalFileName shouldBe "guide.pdf"
                    result.url shouldBe "https://cdn.example.com${result.path}"
                    result.path shouldStartWith "/dymit/"
                    result.thumbnail.shouldBeNull()
                    savedStatuses.shouldContainExactly(
                        UserFileStatus.REQUESTED,
                        UserFileStatus.UPLOADED
                    )
                    savedFiles.first().thumbnailPath.shouldBeNull()
                    verify(exactly = 1) { s3FileService.upload(any(), any()) }
                }
            }

            When("원본 파일 업로드가 실패하면") {
                Then("파일 상태를 FAILED 로 갱신하고 예외를 던진다") {
                    val multipartFile = createPdfMultipartFile()
                    val command = FileUploadCommand(file = multipartFile).enforceFileApiPolicy()
                    val savedStatuses = mutableListOf<UserFileStatus>()

                    stubRepositorySave(savedStatuses)
                    every { s3FileService.upload(any(), any()) } throws RuntimeException("S3 failure")

                    shouldThrow<InternalServerError> {
                        useCase.uploadFile(loginMember, command)
                    }
                    savedStatuses.shouldContainExactly(
                        UserFileStatus.REQUESTED,
                        UserFileStatus.FAILED
                    )
                }
            }
        }

        Given("orientation 메타데이터가 포함된 JPEG 파일 업로드 요청이 주어지면") {
            When("원본 파일과 썸네일 업로드가 모두 성공하면") {
                Then("썸네일은 메타데이터 기준 표시 방향으로 보정된다") {
                    val multipartFile = createJpegMultipartFileWithOrientation(
                        orientationValue = 6,
                        width = 120,
                        height = 60
                    )
                    val command = FileUploadCommand(file = multipartFile).enforceFileApiPolicy()
                    val savedStatuses = mutableListOf<UserFileStatus>()
                    var uploadedThumbnailSize: Pair<Int, Int>? = null

                    stubRepositorySave(savedStatuses)
                    every { s3FileService.upload(any(), any()) } answers {
                        val uploadedFile = firstArg<MultipartFile>()
                        val path = secondArg<String>()
                        if ( path.startsWith("/dymit/thumbnails/") ) {
                            uploadedThumbnailSize = readImageSize(uploadedFile.bytes)
                        }
                        FileUploadResult(
                            path = path,
                            accessUrl = "https://origin.example.com$path"
                        )
                    }

                    val result = useCase.uploadFile(loginMember, command)

                    result.status shouldBe UserFileStatus.UPLOADED
                    uploadedThumbnailSize shouldBe Pair(60, 120)
                    savedStatuses.shouldContainExactly(
                        UserFileStatus.REQUESTED,
                        UserFileStatus.UPLOADED
                    )
                    verify(exactly = 2) { s3FileService.upload(any(), any()) }
                }
            }
        }

        Given("PNG 파일 업로드 요청이 주어지면") {
            When("원본 파일과 썸네일 업로드가 모두 성공하면") {
                Then("이미지로 처리하여 썸네일 메타데이터를 저장하고 응답에 포함한다") {
                    val multipartFile = createPngMultipartFile()
                    val command = FileUploadCommand(file = multipartFile).enforceFileApiPolicy()
                    val savedStatuses = mutableListOf<UserFileStatus>()
                    val savedFiles = mutableListOf<UserFile>()
                    val uploadedPaths = mutableListOf<String>()

                    stubRepositorySave(savedStatuses, savedFiles)
                    every { s3FileService.upload(any(), any()) } answers {
                        val path = secondArg<String>()
                        uploadedPaths.add(path)
                        FileUploadResult(
                            path = path,
                            accessUrl = "https://origin.example.com$path"
                        )
                    }

                    val result = useCase.uploadFile(loginMember, command)

                    result.status shouldBe UserFileStatus.UPLOADED
                    result.originalFileName shouldBe "thumbnail-source.png"
                    result.url shouldBe "https://cdn.example.com${result.path}"
                    result.thumbnail?.path shouldStartWith "/dymit/thumbnails/"
                    result.thumbnail?.url shouldBe "https://cdn.example.com${result.thumbnail!!.path}"
                    uploadedPaths.size shouldBe 2
                    uploadedPaths[0] shouldStartWith "/dymit/"
                    uploadedPaths[1] shouldStartWith "/dymit/thumbnails/"
                    savedStatuses.shouldContainExactly(
                        UserFileStatus.REQUESTED,
                        UserFileStatus.UPLOADED
                    )
                    savedFiles.first().thumbnailPath shouldStartWith "/dymit/thumbnails/"
                    verify(exactly = 2) { s3FileService.upload(any(), any()) }
                }
            }
        }

        Given("지원하지 않는 매직 넘버를 가진 파일 업로드 요청이 주어지면") {
            When("유즈케이스가 파일 형식을 검증하면") {
                Then("BadRequestException 을 던지고 영속화나 업로드를 진행하지 않는다") {
                    val multipartFile = MockMultipartFile(
                        "file",
                        "forged.jpg",
                        "image/jpeg",
                        byteArrayOf(
                            0x47.toByte(),
                            0x49.toByte(),
                            0x46.toByte(),
                            0x38.toByte(),
                            0x39.toByte(),
                            0x61.toByte(),
                            0x00.toByte(),
                            0x00.toByte()
                        )
                    )
                    val command = FileUploadCommand(file = multipartFile).enforceFileApiPolicy()

                    shouldThrow<BadRequestException> {
                        useCase.uploadFile(loginMember, command)
                    }

                    verify(exactly = 0) { userFileRepository.save(any()) }
                    verify(exactly = 0) { s3FileService.upload(any(), any()) }
                }
            }
        }

    }

    private fun stubRepositorySave(
        savedStatuses: MutableList<UserFileStatus>,
        savedFiles: MutableList<UserFile> = mutableListOf()
    ) {
        every { userFileRepository.save(any()) } answers {
            val userFile = firstArg<UserFile>()
            savedStatuses.add(userFile.status)
            savedFiles.add(snapshotUserFile(userFile))
            if ( userFile.id == null ) {
                createPersistedUserFile(userFile, ObjectId.get())
            } else {
                userFile
            }
        }
    }

    private fun createPersistedUserFile(userFile: UserFile, id: ObjectId): UserFile {
        return UserFile(
            id = id,
            memberId = userFile.memberId,
            originalFileName = userFile.originalFileName,
            storedFileName = userFile.storedFileName,
            path = userFile.path,
            thumbnailPath = userFile.thumbnailPath,
            status = userFile.status,
            contentType = userFile.contentType,
            fileSize = userFile.fileSize,
            createdAt = userFile.createdAt,
            updatedAt = userFile.updatedAt,
            isDeleted = userFile.isDeleted
        )
    }

    private fun snapshotUserFile(userFile: UserFile): UserFile {
        return UserFile(
            id = userFile.id,
            memberId = userFile.memberId,
            originalFileName = userFile.originalFileName,
            storedFileName = userFile.storedFileName,
            path = userFile.path,
            thumbnailPath = userFile.thumbnailPath,
            status = userFile.status,
            contentType = userFile.contentType,
            fileSize = userFile.fileSize,
            createdAt = userFile.createdAt,
            updatedAt = userFile.updatedAt,
            isDeleted = userFile.isDeleted
        )
    }

    private fun createJpegBytes(
        width: Int = 640,
        height: Int = 480,
        color: Color = Color(12, 34, 56)
    ): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = color
        graphics.fillRect(0, 0, image.width, image.height)
        graphics.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", outputStream)
        return outputStream.toByteArray()
    }

    private fun createJpegWithOrientationBytes(
        orientationValue: Int,
        width: Int,
        height: Int
    ): ByteArray {
        val jpegBytes = createJpegBytes(
            width = width,
            height = height,
            color = Color(120, 80, 40)
        )
        val exifSegmentData = createExifOrientationSegmentData(orientationValue)
        val segmentLength = exifSegmentData.size + 2
        val outputStream = ByteArrayOutputStream()

        outputStream.write(jpegBytes, 0, 2)
        outputStream.write(0xFF)
        outputStream.write(0xE1)
        outputStream.write(segmentLength shr 8)
        outputStream.write(segmentLength and 0xFF)
        outputStream.write(exifSegmentData)
        outputStream.write(jpegBytes, 2, jpegBytes.size - 2)

        return outputStream.toByteArray()
    }

    private fun createExifOrientationSegmentData(orientationValue: Int): ByteArray {
        val orientationHigh = ((orientationValue shr 8) and 0xFF).toByte()
        val orientationLow = (orientationValue and 0xFF).toByte()

        return byteArrayOf(
            0x45.toByte(),
            0x78.toByte(),
            0x69.toByte(),
            0x66.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x4D.toByte(),
            0x4D.toByte(),
            0x00.toByte(),
            0x2A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x12.toByte(),
            0x00.toByte(),
            0x03.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            orientationHigh,
            orientationLow,
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
    }

    private fun readImageSize(imageBytes: ByteArray): Pair<Int, Int> {
        val image = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: error("이미지 디코딩 결과가 null 입니다.")
        return image.width to image.height
    }

    private fun createPngBytes(): ByteArray {
        val image = BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = Color(98, 76, 54)
        graphics.fillRect(0, 0, image.width, image.height)
        graphics.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }

    private fun createPdfBytes(): ByteArray {
        return "%PDF-1.7\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".toByteArray()
    }

    private fun createJpegMultipartFile(): MockMultipartFile {
        return MockMultipartFile(
            "file",
            "thumbnail-source.jpg",
            "image/jpeg",
            createJpegBytes()
        )
    }

    private fun createJpegMultipartFileWithOrientation(
        orientationValue: Int,
        width: Int,
        height: Int
    ): MockMultipartFile {
        return MockMultipartFile(
            "file",
            "thumbnail-oriented.jpg",
            "image/jpeg",
            createJpegWithOrientationBytes(
                orientationValue = orientationValue,
                width = width,
                height = height
            )
        )
    }

    private fun createPdfMultipartFile(): MockMultipartFile {
        return MockMultipartFile(
            "file",
            "guide.pdf",
            "application/pdf",
            createPdfBytes()
        )
    }

    private fun createPngMultipartFile(): MockMultipartFile {
        return MockMultipartFile(
            "file",
            "thumbnail-source.png",
            "image/png",
            createPngBytes()
        )
    }
}
