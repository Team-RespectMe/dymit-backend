package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleAttachmentDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.controllers.files.dto.FileThumbnailResponse
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import java.time.LocalDateTime

/**
 * 스터디 일정 첨부 파일 응답 DTO입니다.
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
@Schema(description = "스터디 일정 첨부 파일 응답")
class StudyScheduleAttachmentResponse(
    @Schema(description = "파일 ID")
    val fileId: String,
    @Schema(description = "원본 파일명")
    val originalFileName: String,
    @Schema(description = "파일 MIME 타입", nullable = true)
    val contentType: String?,
    @Schema(description = "파일 크기(byte)", example = "2048")
    val fileSize: Long,
    @Schema(description = "파일 상대 경로", example = "/dymit/A/B/UUID_2026_04_27_21_30_10.pdf")
    val path: String,
    @Schema(description = "파일 접근 URL", example = "https://cdn.example.com/dymit/A/B/UUID_2026_04_27_21_30_10.pdf")
    val url: String,
    @Schema(description = "썸네일 정보", nullable = true)
    val thumbnail: FileThumbnailResponse? = null,
    @Schema(description = "파일 상태")
    val status: UserFileStatus,
    @Schema(description = "첨부 생성 시각", example = "2030-10-01T10:00:00")
    val attachedAt: LocalDateTime
): BaseResponse() {

    companion object {

        /**
         * 애플리케이션 DTO를 응답 DTO로 변환합니다.
         *
         * @param dto 변환할 첨부 DTO
         * @return 변환된 응답 DTO
         */
        fun from(dto: StudyScheduleAttachmentDto): StudyScheduleAttachmentResponse {
            return StudyScheduleAttachmentResponse(
                fileId = dto.fileId,
                originalFileName = dto.originalFileName,
                contentType = dto.contentType,
                fileSize = dto.fileSize,
                path = dto.path,
                url = dto.url,
                thumbnail = dto.thumbnail?.let(FileThumbnailResponse::from),
                status = dto.status,
                attachedAt = dto.attachedAt
            )
        }
    }
}
