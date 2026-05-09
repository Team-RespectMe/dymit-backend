package net.noti_me.dymit.dymit_backend_api.application.file.impl

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileDto
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadCommand
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UploadFileUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.InternalServerError
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFile
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.math.roundToInt

/**
 * 파일 업로드 유즈케이스 구현체입니다.
 *
 * JPEG/JPG/PNG/PDF 파일만 허용하며, 이미지 업로드 시 JDK ImageIO를 이용해
 * 썸네일을 생성한 뒤 별도 경로에 함께 업로드합니다.
 *
 * @param userFileRepository 파일 메타데이터 저장소
 * @param s3FileService S3 스트림 업로드 서비스
 * @param cdnConfig CDN 설정 정보
 */
@Service
class UploadFileUseCaseImpl(
    private val userFileRepository: UserFileRepository,
    private val s3FileService: S3FileService,
    private val cdnConfig: CDNConfig
) : UploadFileUseCase {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")

    override fun uploadFile(loginMember: MemberInfo, command: FileUploadCommand): FileDto {
        val file = command.file
        if ( file.isEmpty ) {
            throw BadRequestException(message = "업로드할 파일이 비어 있습니다.")
        }

        val originalFileName = resolveOriginalFileName(file.originalFilename)
        val uploadPreparation = if ( command.enforceFileApiPolicy ) {
            prepareFileApiUpload(
                file = file,
                originalFileName = originalFileName,
                command = command
            )
        } else {
            prepareDefaultUpload(file, originalFileName)
        }

        var userFile = userFileRepository.save(
            UserFile(
                memberId = ObjectId(loginMember.memberId),
                originalFileName = originalFileName,
                storedFileName = uploadPreparation.storageSpec.storedFileName,
                path = uploadPreparation.storageSpec.path,
                thumbnailPath = uploadPreparation.thumbnailUpload?.path,
                status = UserFileStatus.REQUESTED,
                contentType = uploadPreparation.contentType,
                fileSize = uploadPreparation.fileSize
            )
        )

        return try {
            s3FileService.upload(
                file = uploadPreparation.originalFile,
                path = uploadPreparation.storageSpec.path
            )

            uploadPreparation.thumbnailUpload?.let { thumbnailUpload ->
                s3FileService.upload(
                    file = thumbnailUpload.file,
                    path = thumbnailUpload.path
                )
            }

            userFile.updateStatus(UserFileStatus.UPLOADED)
            userFile = userFileRepository.save(userFile)

            FileDto.from(
                userFile = userFile,
                url = buildAccessUrl(userFile.path),
                thumbnailUrl = buildThumbnailAccessUrl(userFile.thumbnailPath)
            )
        } catch (exception: Exception) {
            userFile.updateStatus(UserFileStatus.FAILED)
            userFileRepository.save(userFile)
            throw InternalServerError(message = "파일 업로드에 실패했습니다.")
        }
    }

    /**
     * File API 업로드 정책이 적용된 업로드 준비 정보를 생성합니다.
     *
     * @param file 업로드할 파일
     * @param originalFileName 정규화된 원본 파일 이름
     * @return 업로드 준비 정보
     */
    private fun prepareFileApiUpload(
        file: MultipartFile,
        originalFileName: String,
        command: FileUploadCommand
    ): UploadPreparation {
        val fileBytes = try {
            file.bytes
        } catch (exception: Exception) {
            throw InternalServerError(message = "업로드 파일을 읽는 중 오류가 발생했습니다.")
        }

        val supportedFileType = detectSupportedFileType(fileBytes, command)
            ?: throw BadRequestException(message = resolveUnsupportedFileTypeMessage(command))

        val storageSpec = createStorageSpec(supportedFileType.extension)
        val thumbnailUpload = if ( supportedFileType.isImage ) {
            val thumbnailStorageSpec = createThumbnailStorageSpec(storageSpec)
            val thumbnailBytes = createThumbnailBytes(fileBytes)

            ThumbnailUpload(
                path = thumbnailStorageSpec.path,
                file = InMemoryMultipartFile(
                    name = "thumbnail",
                    originalFileName = thumbnailStorageSpec.storedFileName,
                    contentType = SupportedFileType.JPEG.contentType,
                    bytes = thumbnailBytes
                )
            )
        } else {
            null
        }

        return UploadPreparation(
            originalFile = InMemoryMultipartFile(
                name = file.name,
                originalFileName = originalFileName,
                contentType = supportedFileType.contentType,
                bytes = fileBytes
            ),
            storageSpec = storageSpec,
            contentType = supportedFileType.contentType,
            fileSize = fileBytes.size.toLong(),
            thumbnailUpload = thumbnailUpload
        )
    }

    /**
     * 공용 업로드 흐름에서 사용하는 기본 업로드 준비 정보를 생성합니다.
     *
     * @param file 업로드할 파일
     * @param originalFileName 정규화된 원본 파일 이름
     * @return 업로드 준비 정보
     */
    private fun prepareDefaultUpload(
        file: MultipartFile,
        originalFileName: String
    ): UploadPreparation {
        return UploadPreparation(
            originalFile = file,
            storageSpec = createStorageSpec(extractExtension(originalFileName)),
            contentType = file.contentType,
            fileSize = file.size,
            thumbnailUpload = null
        )
    }

    /**
     * 매직 넘버를 기준으로 허용된 파일 형식을 판별합니다.
     *
     * @param fileBytes 업로드 파일 바이트 배열
     * @param command 업로드 요청 커맨드
     * @return 허용된 파일 형식, 아니면 null
     */
    private fun detectSupportedFileType(
        fileBytes: ByteArray,
        command: FileUploadCommand
    ): SupportedFileType? {
        val allowedTypes = resolveAllowedFileTypes(command)

        if ( SupportedFileType.JPEG in allowedTypes &&
            fileBytes.size >= JPEG_MAGIC.size &&
            fileBytes.copyOfRange(0, JPEG_MAGIC.size).contentEquals(JPEG_MAGIC)
        ) {
            return SupportedFileType.JPEG
        }

        if ( SupportedFileType.PNG in allowedTypes &&
            fileBytes.size >= PNG_MAGIC.size &&
            fileBytes.copyOfRange(0, PNG_MAGIC.size).contentEquals(PNG_MAGIC)
        ) {
            return SupportedFileType.PNG
        }

        if ( SupportedFileType.PDF in allowedTypes &&
            fileBytes.size >= PDF_MAGIC.size &&
            fileBytes.copyOfRange(0, PDF_MAGIC.size).contentEquals(PDF_MAGIC)
        ) {
            return SupportedFileType.PDF
        }

        return null
    }

    /**
     * 업로드 진입점에 따라 허용되는 파일 형식 목록을 반환합니다.
     *
     * @param command 업로드 요청 커맨드
     * @return 허용 파일 형식 집합
     */
    private fun resolveAllowedFileTypes(command: FileUploadCommand): Set<SupportedFileType> {
        return if ( command.enforceFileApiPolicy ) {
            setOf(
                SupportedFileType.JPEG,
                SupportedFileType.PNG,
                SupportedFileType.PDF
            )
        } else {
            setOf(SupportedFileType.JPEG)
        }
    }

    /**
     * 업로드 진입점에 맞는 파일 형식 오류 메시지를 반환합니다.
     *
     * @param command 업로드 요청 커맨드
     * @return 사용자 노출용 오류 메시지
     */
    private fun resolveUnsupportedFileTypeMessage(command: FileUploadCommand): String {
        return if ( command.enforceFileApiPolicy ) {
            "지원하지 않는 파일 형식입니다. JPEG/JPG/PNG/PDF만 업로드할 수 있습니다."
        } else {
            "지원하지 않는 파일 형식입니다. JPEG/JPG만 업로드할 수 있습니다."
        }
    }

    /**
     * 이미지 바이트에서 썸네일을 생성합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @return 생성된 썸네일 바이트 배열
     */
    private fun createThumbnailBytes(imageBytes: ByteArray): ByteArray {
        val sourceImage = try {
            ImageIO.read(ByteArrayInputStream(imageBytes))
        } catch (exception: Exception) {
            throw BadRequestException(message = "유효하지 않은 이미지입니다.")
        } ?: throw BadRequestException(message = "유효하지 않은 이미지입니다.")

        val resizedImage = resizeImage(sourceImage)
        val outputStream = ByteArrayOutputStream()
        val isWritten = ImageIO.write(resizedImage, SupportedFileType.JPEG.formatName, outputStream)

        if ( !isWritten ) {
            throw InternalServerError(message = "썸네일 생성에 실패했습니다.")
        }

        return outputStream.toByteArray()
    }

    /**
     * 지정된 최대 크기에 맞춰 이미지를 축소합니다.
     *
     * @param sourceImage 원본 이미지
     * @return 리사이즈된 이미지
     */
    private fun resizeImage(sourceImage: BufferedImage): BufferedImage {
        val scale = minOf(
            THUMBNAIL_MAX_WIDTH.toDouble() / sourceImage.width.toDouble(),
            THUMBNAIL_MAX_HEIGHT.toDouble() / sourceImage.height.toDouble(),
            1.0
        )

        val targetWidth = (sourceImage.width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (sourceImage.height * scale).roundToInt().coerceAtLeast(1)
        val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = resizedImage.createGraphics()

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null)
        graphics.dispose()

        return resizedImage
    }

    /**
     * 원본 파일 저장 경로를 생성합니다.
     *
     * @param fileType 검증된 파일 형식
     * @return 저장 경로 명세
     */
    private fun createStorageSpec(extension: String?): StorageSpec {
        val uuid = UUID.randomUUID().toString().replace("-", "").uppercase(Locale.ROOT)
        val timestamp = LocalDateTime.now().format(formatter)
        val baseName = "${uuid}_${timestamp}"
        val storedFileName = if ( extension.isNullOrBlank() ) {
            baseName
        } else {
            "${baseName}.${extension}"
        }
        val firstDirectory = uuid[0]
        val secondDirectory = uuid[1]

        return StorageSpec(
            baseName = baseName,
            storedFileName = storedFileName,
            path = "/dymit/${firstDirectory}/${secondDirectory}/${storedFileName}",
            firstDirectory = firstDirectory,
            secondDirectory = secondDirectory
        )
    }

    /**
     * 썸네일 저장 경로를 생성합니다.
     *
     * @param storageSpec 원본 파일 저장 경로 명세
     * @return 썸네일 저장 경로 명세
     */
    private fun createThumbnailStorageSpec(storageSpec: StorageSpec): ThumbnailStorageSpec {
        val storedFileName = "${storageSpec.baseName}_thumbnail.${SupportedFileType.JPEG.extension}"

        return ThumbnailStorageSpec(
            storedFileName = storedFileName,
            path = "/dymit/thumbnails/${storageSpec.firstDirectory}/${storageSpec.secondDirectory}/${storedFileName}"
        )
    }

    /**
     * 업로드 파일의 원본 파일 이름을 정규화합니다.
     *
     * @param originalFilename 클라이언트가 전달한 파일 이름
     * @return 정규화된 원본 파일 이름
     */
    private fun resolveOriginalFileName(originalFilename: String?): String {
        if ( originalFilename.isNullOrBlank() ) {
            return "unnamed"
        }

        return originalFilename.substringAfterLast('/').substringAfterLast('\\')
    }

    /**
     * 원본 파일 이름에서 확장자를 추출합니다.
     *
     * @param originalFileName 정규화된 원본 파일 이름
     * @return 확장자, 없으면 null
     */
    private fun extractExtension(originalFileName: String): String? {
        val extension = originalFileName.substringAfterLast('.', "")
        if ( extension.isBlank() || extension == originalFileName ) {
            return null
        }

        return extension
    }

    /**
     * 상대 경로를 CDN 접근 URL로 변환합니다.
     *
     * @param path 상대 경로
     * @return 접근 URL
     */
    private fun buildAccessUrl(path: String): String {
        return cdnConfig.getDomain().trimEnd('/') + path
    }

    /**
     * 썸네일 상대 경로를 CDN 접근 URL로 변환합니다.
     *
     * @param path 썸네일 상대 경로
     * @return 접근 URL
     */
    private fun buildThumbnailAccessUrl(path: String?): String? {
        return path?.let { buildAccessUrl(it) }
    }

    private data class StorageSpec(
        val baseName: String,
        val storedFileName: String,
        val path: String,
        val firstDirectory: Char,
        val secondDirectory: Char
    )

    private data class ThumbnailStorageSpec(
        val storedFileName: String,
        val path: String
    )

    private data class UploadPreparation(
        val originalFile: MultipartFile,
        val storageSpec: StorageSpec,
        val contentType: String?,
        val fileSize: Long,
        val thumbnailUpload: ThumbnailUpload?
    )

    private data class ThumbnailUpload(
        val path: String,
        val file: MultipartFile
    )

    private enum class SupportedFileType(
        val contentType: String,
        val extension: String,
        val formatName: String,
        val isImage: Boolean
    ) {
        JPEG(
            contentType = "image/jpeg",
            extension = "jpg",
            formatName = "jpg",
            isImage = true
        ),
        PNG(
            contentType = "image/png",
            extension = "png",
            formatName = "png",
            isImage = true
        ),
        PDF(
            contentType = "application/pdf",
            extension = "pdf",
            formatName = "pdf",
            isImage = false
        )
    }

    /**
     * 메모리 상의 바이트 배열을 MultipartFile 형태로 감싸는 구현체입니다.
     *
     * @param name 멀티파트 파라미터 이름
     * @param originalFileName 원본 파일 이름
     * @param contentType 파일 MIME 타입
     * @param bytes 파일 바이트 배열
     */
    private class InMemoryMultipartFile(
        private val name: String,
        private val originalFileName: String,
        private val contentType: String,
        private val bytes: ByteArray
    ) : MultipartFile {

        override fun getName(): String = name

        override fun getOriginalFilename(): String = originalFileName

        override fun getContentType(): String = contentType

        override fun isEmpty(): Boolean = bytes.isEmpty()

        override fun getSize(): Long = bytes.size.toLong()

        override fun getBytes(): ByteArray = bytes.copyOf()

        override fun getInputStream() = ByteArrayInputStream(bytes)

        override fun transferTo(dest: File) {
            dest.writeBytes(bytes)
        }
    }

    companion object {

        private val JPEG_MAGIC = byteArrayOf(
            0xFF.toByte(),
            0xD8.toByte(),
            0xFF.toByte()
        )

        private val PNG_MAGIC = byteArrayOf(
            0x89.toByte(),
            0x50.toByte(),
            0x4E.toByte(),
            0x47.toByte(),
            0x0D.toByte(),
            0x0A.toByte(),
            0x1A.toByte(),
            0x0A.toByte()
        )

        private val PDF_MAGIC = byteArrayOf(
            0x25.toByte(),
            0x50.toByte(),
            0x44.toByte(),
            0x46.toByte(),
            0x2D.toByte()
        )

        private const val THUMBNAIL_MAX_WIDTH = 320
        private const val THUMBNAIL_MAX_HEIGHT = 320
    }
}
