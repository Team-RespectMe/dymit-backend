package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.*
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.ScheduleParticipantResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleCommandResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleListItem
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.StudyScheduleResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "Study Schedule API", description = "스터디 그룹 일정 관리 API")
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/v1/study-groups/")
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
        summary = "스터디 그룹 일정 생성",
        description = "로그인한 멤버가 스터디 그룹의 일정을 생성합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "스터디 그룹 일정이 성공적으로 생성되었습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 요청 형식을 확인해주세요."
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자입니다. 로그인 후 다시 시도해주세요."
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한이 없는 사용자입니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 스터디 그룹입니다."
            ),
        ]
    )
    @PostMapping("{groupId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    fun createSchedule(
        @LoginMember
        memberInfo: MemberInfo,
        @PathVariable
        groupId: String,
        @RequestBody @Valid
        request: StudyScheduleCommandRequest
    ): StudyScheduleCommandResponse


    /**
     * 스터디 그룹 일정을 업데이트합니다.
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param scheduleId 업데이트할 스터디 일정 ID
     * @param command 스터디 일정 업데이트 요청
     */
    @Operation(
        summary = "스터디 그룹 일정 업데이트",
        description = "로그인한 멤버가 스터디 그룹의 일정을 업데이트합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스터디 그룹 일정이 성공적으로 업데이트되었습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 요청 형식을 확인해주세요."
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자입니다. 로그인 후 다시 시도해주세요."
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한이 없는 사용자입니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 스터디 그룹 또는 일정입니다."
            ),
        ]
    )
    @PutMapping("{groupId}/schedules/{scheduleId}")
    fun updateSchedule(
        @LoginMember
        memberInfo: MemberInfo,
        @PathVariable
        groupId: String,
        @PathVariable
        scheduleId: String,
        @RequestBody @Valid
        command: StudyScheduleCommandRequest
    ): StudyScheduleCommandResponse

    /**
     * 스터디 그룹 일정을 삭제합니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param scheduleId 삭제할 스터디 일정 ID
     */
    @Operation(
        summary = "스터디 그룹 일정 삭제",
        description = "로그인한 멤버가 스터디 그룹의 일정을 삭제합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "스터디 그룹 일정이 성공적으로 삭제되었습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 요청 형식을 확인해주세요."
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자입니다. 로그인 후 다시 시도해주세요."
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한이 없는 사용자입니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 스터디 그룹 또는 일정입니다."
            ),
        ]
    )
    @DeleteMapping("{groupId}/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeSchedule(
        @LoginMember
        memberInfo: MemberInfo,
        @PathVariable
        groupId: String,
        @PathVariable
        scheduleId: String
    ): Unit

    /**
     * 스터디 그룹의 모든 일정을 조회합니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @return 스터디 그룹의 모든 일정 목록
     */
    @Operation(
        summary = "스터디 그룹 일정 목록 조회",
        description = "로그인한 멤버가 스터디 그룹의 모든 일정을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스터디 그룹의 모든 일정 목록이 성공적으로 조회되었습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 요청 형식을 확인해주세요."
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자입니다. 로그인 후 다시 시도해주세요."
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한이 없는 사용자입니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 스터디 그룹입니다."
            ),
        ]
    )
    @GetMapping("/{groupId}/schedules")
    fun getGroupSchedules(
        @LoginMember
        memberInfo: MemberInfo,
        @PathVariable
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
    @Operation(
        summary = "스터디 그룹 일정 상세 조회",
        description = "로그인한 멤버가 스터디 그룹 일정의 상세 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스터디 그룹 일정의 상세 정보가 성공적으로 조회되었습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 요청 형식을 확인해주세요."
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자입니다. 로그인 후 다시 시도해주세요."
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한이 없는 사용자입니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 스터디 그룹 또는 일정입니다."
            ),
        ]
    )
    @GetMapping("{groupId}/schedules/{scheduleId}")
    fun getScheduleDetail(
        @LoginMember
        memberInfo: MemberInfo,
        @PathVariable
        groupId: String,
        @PathVariable
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
    @Operation(
        summary = "스터디 그룹 일정 참여",
        description = "로그인한 멤버가 스터디 그룹 일정에 참여합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "스터디 그룹 일정에 성공적으로 참여하였습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 요청 형식을 확인해주세요."
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자입니다. 로그인 후 다시 시도해주세요."
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한이 없는 사용자입니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 스터디 그룹 또는 일정입니다."
            ),
        ]
    )
    @PostMapping("{groupId}/schedules/{scheduleId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    fun joinSchedule(
        @LoginMember
        memberInfo: MemberInfo,
        @PathVariable
        groupId: String,
        @PathVariable
        scheduleId: String
    ): ScheduleParticipantResponse

    /**
     * 스터디 그룹 일정에서 나갑니다.
     *
     * @param memberInfo 로그인한 멤버 정보
     * @param groupId 스터디 그룹 ID
     * @param scheduleId 나갈 스터디 일정 ID
     */
    @Operation(
        summary = "스터디 그룹 일정 나가기",
        description = "로그인한 멤버가 스터디 그룹 일정에서 나갑니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "스터디 그룹 일정에서 성공적으로 나갔습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 요청 형식을 확인해주세요."
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자입니다. 로그인 후 다시 시도해주세요."
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한이 없는 사용자입니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 스터디 그룹 또는 일정입니다."
            )
        ]
    )
    @DeleteMapping("{groupId}/schedules/{scheduleId}/participants")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun leaveSchedule(
        @LoginMember
        memberInfo: MemberInfo,
        @PathVariable
        groupId: String,
        @PathVariable
        scheduleId: String
    ): Unit
}