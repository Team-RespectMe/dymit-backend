package net.noti_me.dymit.dymit_backend_api.application.file.impl

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import net.noti_me.dymit.dymit_backend_api.application.file.FileIOService
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadResult
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class S3FileService(
    private val s3: AmazonS3
): FileIOService {

    /**
     * S3 파일 업로드
     * @param fileData 업로드할 파일 데이터
     * @param bucket 업로드할 버킷 이름
     * @param path 업로드할 경로
     * @return 업로드된 파일 정보
     * @throws java.io.IOException 파일 업로드 중 오류 발생 시, 반드시 처리해야함
     */
    override fun putFile(
        fileData: MultipartFile,
        bucket: String,
        path: String
    ): FileUploadResult {
        val metadata = ObjectMetadata().apply {
            contentLength = fileData.size
            contentType = fileData.contentType
        }

        s3.putObject(bucket, path, fileData.inputStream, metadata)
        val url = s3.getUrl(bucket, path).toString()

        return FileUploadResult(
            bucket = bucket,
            key = path,
            url = url
        )
    }

    /**
     * S3 파일 삭제
     * @param bucket 삭제할 파일이 있는 버킷 이름
     * @param path 삭제할 파일 경로
     */
    override fun deleteFile(bucket: String, path: String) {
        return s3.deleteObject(bucket, path)
    }
}
