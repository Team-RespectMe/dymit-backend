package net.noti_me.dymit.dymit_backend_api.application.file.impl

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadResult
import net.noti_me.dymit.dymit_backend_api.configs.S3Config
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream

/**
 * S3 스트림 업로드를 담당하는 서비스입니다.
 *
 * @param amazonS3 AmazonS3 클라이언트
 * @param s3Config S3 설정 정보
 */
@Service
class S3FileService(
    private val amazonS3: AmazonS3,
    private val s3Config: S3Config
) {

    /**
     * 파일을 스트림 방식으로 S3에 업로드합니다.
     *
     * @param file 업로드할 멀티파트 파일
     * @param path 업로드 대상 상대 경로
     * @return 업로드 결과
     */
    fun upload(file: MultipartFile, path: String): FileUploadResult {
        val key = path.removePrefix("/")
        val metadata = ObjectMetadata().apply {
            contentLength = file.size
            contentType = file.contentType
        }

        file.inputStream.use { inputStream ->
            amazonS3.putObject(s3Config.getBucketName(), key, inputStream, metadata)
        }

        return FileUploadResult(
            path = path,
            accessUrl = s3Config.getOriginDomain().trimEnd('/') + path
        )
    }

    /**
     * 바이트 배열을 S3에 업로드합니다.
     *
     * @param content 업로드할 파일 바이트 배열
     * @param contentType 업로드할 파일 MIME 타입
     * @param path 업로드 대상 상대 경로
     * @return 업로드 결과
     */
    fun upload(
        content: ByteArray,
        contentType: String?,
        path: String
    ): FileUploadResult {
        val key = path.removePrefix("/")
        val metadata = ObjectMetadata().apply {
            contentLength = content.size.toLong()
            this.contentType = contentType
        }

        ByteArrayInputStream(content).use { inputStream ->
            amazonS3.putObject(s3Config.getBucketName(), key, inputStream, metadata)
        }

        return FileUploadResult(
            path = path,
            accessUrl = s3Config.getOriginDomain().trimEnd('/') + path
        )
    }
}
