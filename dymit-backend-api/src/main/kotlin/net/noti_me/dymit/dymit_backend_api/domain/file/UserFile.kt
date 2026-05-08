package net.noti_me.dymit.dymit_backend_api.domain.file

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 업로드된 사용자 파일 메타데이터를 저장하는 도메인 엔티티입니다.
 *
 * @param memberId 업로드를 요청한 멤버 ID
 * @param originalFileName 사용자가 업로드한 실제 파일 이름
 * @param storedFileName S3에 저장된 파일 이름
 * @param path CDN 또는 S3 도메인을 제외한 상대 경로
 * @param thumbnailPath 썸네일 상대 경로
 * @param status 파일 처리 상태
 * @param contentType 파일 MIME 타입
 * @param fileSize 파일 크기(byte)
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param isDeleted 삭제 여부
 * @param id 문서 ID
 */
@Document(collection = "user_files")
class UserFile(
    val memberId: ObjectId,
    val originalFileName: String,
    val storedFileName: String,
    path: String,
    thumbnailPath: String? = null,
    status: UserFileStatus = UserFileStatus.REQUESTED,
    val contentType: String? = null,
    val fileSize: Long = 0L,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false,
    id: ObjectId? = null
) : BaseAggregateRoot<UserFile>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    var path: String = path
        private set

    var thumbnailPath: String? = thumbnailPath
        private set

    var status: UserFileStatus = status
        private set

    /**
     * 파일 상태를 갱신합니다.
     *
     * @param newStatus 변경할 상태
     * @return Unit
     */
    fun updateStatus(newStatus: UserFileStatus) {
        this.status = newStatus
    }

    /**
     * 파일 썸네일 경로를 갱신합니다.
     *
     * @param newThumbnailPath 변경할 썸네일 경로
     * @return Unit
     */
    fun updateThumbnailPath(newThumbnailPath: String?) {
        this.thumbnailPath = newThumbnailPath
    }
}
