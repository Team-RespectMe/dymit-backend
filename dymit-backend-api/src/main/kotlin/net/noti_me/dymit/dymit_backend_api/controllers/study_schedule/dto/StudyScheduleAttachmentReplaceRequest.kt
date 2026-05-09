package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ReplaceStudyScheduleAttachmentsCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import org.bson.types.ObjectId

/**
 * 스터디 일정 첨부 목록 전체 교체 요청 DTO입니다.
 *
 * @param fileIds 최종 첨부 파일 ID 목록
 */
@Schema(description = "스터디 일정 첨부 목록 전체 교체 요청")
@Sanitize
class StudyScheduleAttachmentReplaceRequest(
    @field:Schema(
        description = "최종 첨부 파일 ID 목록",
        example = "[\"688c25eb2f3a71dcf291aac9\", \"688c25eb2f3a71dcf291aaca\"]"
    )
    val fileIds: List<String> = emptyList()
) {

    /**
     * 모든 파일 ID가 유효한 ObjectId 형식인지 검증합니다.
     *
     * @return 검증 결과
     */
    @AssertTrue(message = "fileIds에는 유효한 ObjectId만 포함할 수 있습니다.")
    fun hasValidFileIds(): Boolean {
        return fileIds.all(ObjectId::isValid)
    }

    /**
     * 서비스 계층 커맨드로 변환합니다.
     *
     * @param scheduleId 대상 스케줄 ID
     * @return 변환된 커맨드
     */
    fun toCommand(scheduleId: String): ReplaceStudyScheduleAttachmentsCommand {
        return ReplaceStudyScheduleAttachmentsCommand(
            scheduleId = scheduleId,
            fileIds = fileIds
        )
    }
}
