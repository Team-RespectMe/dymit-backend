package net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto.StudyScheduleFileDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto.StudyScheduleFileStatusDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleAttachment
import java.time.Instant

/**
 * 스터디 일정 첨부 파일 정보를 외부로 전달하는 DTO입니다.
 *
 * @param fileId 파일 ID
 * @param originalFileName 원본 파일명
 * @param contentType MIME 타입
 * @param fileSize 파일 크기(byte)
 * @param path 파일 상대 경로
 * @param url 파일 접근 URL
 * @param thumbnail 썸네일 정보
 * @param status 파일 상태
 * @param attachedAt 첨부 생성 시각
 */
data class StudyScheduleAttachmentDto(
    val fileId: String,
    val originalFileName: String,
    val contentType: String?,
    val fileSize: Long,
    val path: String,
    val url: String,
    val thumbnail: StudyScheduleFileThumbnailDto? = null,
    val status: StudyScheduleFileStatusDto,
    val attachedAt: Instant
) {

    companion object {

        /**
         * 첨부 관계와 파일 메타데이터를 DTO로 변환합니다.
         *
         * @param attachment 첨부 관계 엔티티
         * @param userFile 파일 엔티티
         * @param url 파일 접근 URL
         * @param thumbnailUrl 썸네일 접근 URL
         * @return 변환된 DTO
         */
        fun from(
            attachment: ScheduleAttachment,
            userFile: StudyScheduleFileDto
        ): StudyScheduleAttachmentDto {
            return StudyScheduleAttachmentDto(
                fileId = userFile.id.toHexString(),
                originalFileName = userFile.originalFileName,
                contentType = userFile.contentType,
                fileSize = userFile.fileSize,
                path = userFile.path,
                url = userFile.url,
                thumbnail = if ( userFile.thumbnailPath != null && userFile.thumbnailUrl != null ) {
                    StudyScheduleFileThumbnailDto(
                        path = userFile.thumbnailPath!!,
                        url = userFile.thumbnailUrl!!
                    )
                } else {
                    null
                },
                status = userFile.status,
                attachedAt = attachment.createdAt ?: Instant.now()
            )
        }
    }
}

data class StudyScheduleFileThumbnailDto(
    val path: String,
    val url: String
)
