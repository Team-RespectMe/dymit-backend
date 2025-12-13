package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.*

@Tag(name = "스터디 일정 API", description = "스터디 그룹 일정 관리 API")
@SecurityRequirement(name = "bearer-jwt")
interface StudyScheduleApi {


    /**
     * 스터디 그룹 일정을 생성합니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param request 스터디 일정 생성 요청
     * @return 생성된 스터디 일정 정보
     */
    @Operation(
        method = "POST",
        summary = "스터디 그룹 일정 생성",
        description = "로그인한 멤버가 스터디 그룹의 일정을 생성합니다."
    )
    @ApiResponse(responseCode = "201", description = "스터디 그룹 일정이 성공적으로 생성되었습니다.")
    fun createSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        @Valid request: StudyScheduleCommandRequest
    ): StudyScheduleCommandResponse


    /**
     * 스터디 그룹 일정을 업데이트합니다.
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param scheduleId 업데이트할 스터디 일정 ID
     * @param command 스터디 일정 업데이트 요청
     */
    @Operation(
        method = "PUT",
        summary = "스터디 그룹 일정 업데이트",
        description = "로그인한 멤버가 스터디 그룹의 일정을 업데이트합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "스터디 그룹 일정이 성공적으로 업데이트되었습니다."
    )
    fun updateSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String,
        @Valid request: StudyScheduleCommandRequest
    ): StudyScheduleCommandResponse

    /**
     * 스터디 그룹 일정을 삭제합니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param scheduleId 삭제할 스터디 일정 ID
     */
    @Operation(summary = "스터디 그룹 일정 삭제", description = "로그인한 멤버가 스터디 그룹의 일정을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 일정이 성공적으로 삭제되었습니다.")
    fun removeSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    )

    /**
     * 스터디 그룹의 모든 일정을 조회합니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @return 스터디 그룹의 모든 일정 목록
     */
    @Operation(summary = "스터디 그룹 일정 목록 조회", description = "로그인한 멤버가 스터디 그룹의 모든 일정을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "스터디 그룹의 모든 일정 목록이 성공적으로 조회되었습니다.")
    fun getGroupSchedules(
        memberInfo: MemberInfo,
        groupId: String
    ): ListResponse<StudyScheduleListItem>

    /**
     * 스터디 그룹 일정의 상세 정보를 조회합니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param scheduleId 조회할 스터디 일정 ID
     * @return 스터디 그룹 일정의 상세 정보
     */
    @Operation(summary = "스터디 그룹 일정 상세 조회", description = "로그인한 멤버가 스터디 그룹 일정의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "스터디 그룹 일정의 상세 정보가 성공적으로 조회되었습니다.")
    fun getScheduleDetail(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): StudyScheduleResponse

    /**
     * 스터디 그룹 일정에 참여합니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param scheduleId 참여할 스터디 일정 ID
     * @return 참여한 스터디 일정의 정보
     */
    @Operation(summary = "스터디 그룹 일정 참여", description = "로그인한 멤버가 스터디 그룹 일정에 참여합니다.")
    @ApiResponse(responseCode = "201", description = "스터디 그룹 일정에 성공적으로 참여하였습니다.")
    fun joinSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): ScheduleParticipantResponse

    /**
     * 스터디 그룹 일정에서 나갑니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param scheduleId 나갈 스터디 일정 ID
     */
    @Operation(summary = "스터디 그룹 일정 나가기", description = "로그인한 멤버가 스터디 그룹 일정에서 나갑니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 일정에서 성공적으로 나갔습니다.")

    fun leaveSchedule(
        memberInfo: MemberInfo,
        groupId: String,
        scheduleId: String
    ): Unit
}