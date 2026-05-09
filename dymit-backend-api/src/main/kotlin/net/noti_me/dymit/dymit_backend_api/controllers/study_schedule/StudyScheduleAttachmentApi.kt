package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleAttachmentReplaceRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleAttachmentResponse

/**
 * 스터디 일정 첨부 파일 API 인터페이스입니다.
 */
@Tag(name = "스터디 일정 첨부파일 API", description = "스터디 일정 첨부파일 관리 API")
@SecurityRequirement(name = "bearer-jwt")
interface StudyScheduleAttachmentApi {

    /**
     * 일정의 첨부 목록 전체를 교체합니다.
     *
     * @param memberInfo 로그인 멤버 정보
     * @param scheduleId 대상 일정 ID
     * @param request 최종 첨부 목록 요청
     * @return 최종 첨부 목록
     */
    @Operation(
        method = "PUT",
        summary = "스터디 일정 첨부파일 전체 교체",
        description = "로그인한 멤버가 스터디 일정의 첨부파일 목록 전체를 교체합니다."
    )
    @ApiResponse(responseCode = "200", description = "스터디 일정 첨부파일이 성공적으로 교체되었습니다.")
    fun replaceAttachments(
        memberInfo: MemberInfo,
        scheduleId: String,
        @Valid request: StudyScheduleAttachmentReplaceRequest
    ): ListResponse<StudyScheduleAttachmentResponse>

    /**
     * 일정의 첨부 목록을 조회합니다.
     *
     * @param memberInfo 로그인 멤버 정보
     * @param scheduleId 대상 일정 ID
     * @return 현재 첨부 목록
     */
    @Operation(
        method = "GET",
        summary = "스터디 일정 첨부파일 조회",
        description = "로그인한 멤버가 스터디 일정의 현재 첨부파일 목록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "스터디 일정 첨부파일이 성공적으로 조회되었습니다.")
    fun getAttachments(
        memberInfo: MemberInfo,
        scheduleId: String
    ): ListResponse<StudyScheduleAttachmentResponse>
}
