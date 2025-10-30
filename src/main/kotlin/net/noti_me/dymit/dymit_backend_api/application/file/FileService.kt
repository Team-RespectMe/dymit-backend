package net.noti_me.dymit.dymit_backend_api.application.file

import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadResult
import org.springframework.web.multipart.MultipartFile

interface FileIOService {

    /**
     * 파일 업로드
     * @param fileData 업로드할 파일 데이터
     * @param bucket 업로드할 버킷 이름
     * @param path 업로드할 경로
     * @return 업로드된 파일 정보
     */
    fun putFile(
        fileData: MultipartFile,
        bucket: String,
        path: String
    ): FileUploadResult

    /**
     * 파일 삭제
     * @param bucket 삭제할 파일이 있는 버킷 이름
     * @param path 삭제할 파일 경로
     */
    fun deleteFile(
        bucket: String,
        path: String
    )
}
