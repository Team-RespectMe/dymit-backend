package net.noti_me.dymit.dymit_backend_api.calendar.application.port.`in`.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.`in`.web.dto.CalendarDayPresenceItem
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.`in`.web.dto.CalendarEventItem
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 로그인 회원의 캘린더 조회 웹 API입니다.
 */
@Tag(name = "캘린더 API", description = "로그인 회원의 참가 일정과 제출 대상 과제 캘린더 API")
@SecurityRequirement(name = "bearer-jwt")
interface CalendarApi {

    /**
     * 기준일이 속한 일요일부터 7일간 이벤트 존재 여부를 조회합니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param date ISO 기준일 문자열
     * @return 날짜순 7일의 캘린더 응답
     */
    @Operation(
        summary = "주간 캘린더 이벤트 존재 여부 조회",
        description = "서울 시간 기준으로 본인이 참가한 일정과 제출 대상 과제를 일요일부터 다음 일요일 직전까지 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "400", description = "날짜 형식 또는 조회 범위 오류")
    fun getWeeklyPresence(
        memberInfo: MemberInfo,
        @Parameter(description = "ISO 날짜. 생략하면 서울 기준 오늘", example = "2026-10-02")
        date: String?
    ): ListResponse<CalendarDayPresenceItem>

    /**
     * 지정한 서울 날짜의 이벤트를 평탄한 목록으로 조회합니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param date ISO 조회일 문자열
     * @return 그룹 식별자와 이벤트 시각순 이벤트 응답
     */
    @Operation(
        summary = "일별 캘린더 이벤트 조회",
        description = "서울 시간 기준 하루의 참가 일정과 제출 대상 과제를 그룹 오름차순, 이벤트 시각 내림차순으로 조회합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(
                name = "TASK와 STUDY_SCHEDULE 예시",
                value = """{
                  "status": 200,
                  "data": {
                    "count": 2,
                    "items": [
                      {
                        "type": "TASK",
                        "id": "66fdb34705af234c41b32d10",
                        "title": "주간 과제",
                        "eventAt": "2026-10-02T10:00:00Z",
                        "group": { "id": "66fdb34705af234c41b32d01", "name": "알고리즘 스터디" },
                        "submissionType": "OUTPUT"
                      },
                      {
                        "type": "STUDY_SCHEDULE",
                        "id": "66fdb34705af234c41b32d11",
                        "title": "정기 모임",
                        "eventAt": "2026-10-02T11:00:00Z",
                        "group": { "id": "66fdb34705af234c41b32d01", "name": "알고리즘 스터디" },
                        "location": { "type": "ONLINE", "value": "화상 회의", "link": "https://example.com/meeting" }
                      }
                    ],
                    "_links": {}
                  },
                  "traceId": null
                }"""
            )]
        )]
    )
    @ApiResponse(responseCode = "400", description = "날짜 형식 또는 조회 범위 오류")
    fun getDailyEvents(
        memberInfo: MemberInfo,
        @Parameter(description = "ISO 날짜. 생략하면 서울 기준 오늘", example = "2026-10-02")
        date: String?
    ): ListResponse<CalendarEventItem>
}
