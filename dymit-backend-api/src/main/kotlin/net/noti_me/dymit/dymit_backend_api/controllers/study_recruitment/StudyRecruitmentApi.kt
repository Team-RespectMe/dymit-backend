package net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment.dto.QueryStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment.dto.StudyRecruitmentResponse
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 스터디 모집 목록 조회 API 인터페이스입니다.
 *
 * 첫 번째 파라미터로 로그인 사용자 정보를 전달받고,
 * 커서 기반 페이징으로 목록을 조회합니다.
 */
@Tag(name = "스터디 모집 API", description = "스터디 모집 관련 API")
@RequestMapping("/api/v1/study-recruitments")
@SecurityRequirement(name = "bearer-jwt")
interface StudyRecruitmentApi {

    /**
     * 스터디 모집 목록을 조회합니다.
     *
     * @param memberInfo 로그인 사용자 정보
     * @param cursor 다음 페이지 조회를 위한 커서
     * @param size 조회 개수
     * @return 커서 기반 목록 응답
     */
    @ApiResponse(responseCode = "200", description = "스터디 모집 목록을 성공적으로 조회했습니다.")
    @Operation(method = "GET", summary = "스터디 모집 목록 조회", description = "커서 기반으로 스터디 모집 목록을 조회합니다.")
    fun getStudyRecruitments(
        memberInfo: MemberInfo,
        cursor: String? = null,
        size: Int = 20
    ): ListResponse<StudyRecruitmentResponse>
}

