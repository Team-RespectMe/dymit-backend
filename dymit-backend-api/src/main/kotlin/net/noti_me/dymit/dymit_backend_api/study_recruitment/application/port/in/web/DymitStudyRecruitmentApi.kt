package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.CreateStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentSummaryResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.StudyRecruitmentRequestType
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.UpdateStudyRecruitmentRequest
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Dymit 스터디 모집글 v2 웹 API입니다.
 */
@Tag(name = "Dymit 스터디 모집 API", description = "Dymit 스터디 모집글 CRUD API")
@RequestMapping("/api/v2/study-recruitments")
interface DymitStudyRecruitmentApi {

    /**
     * Dymit 스터디 모집글을 생성합니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param request 모집글 생성 요청
     * @return 생성된 모집글 응답
     */
    @ApiResponse(responseCode = "201", description = "모집글을 생성했습니다.")
    @Operation(method = "POST", summary = "Dymit 스터디 모집글 생성")
    @SecurityRequirement(name = "bearer-jwt")
    fun createStudyRecruitment(
        memberInfo: MemberInfo,
        @Valid request: CreateStudyRecruitmentRequest
    ): DymitStudyRecruitmentResponse

    /**
     * Dymit 스터디 모집글 목록을 조회합니다.
     *
     * @param cursor 다음 페이지 조회를 위한 커서
     * @param size 조회 개수
     * @param type 조회할 모집글 유형
     * @param mine 본인이 작성한 Dymit 모집글만 조회할지 여부
     * @param memberInfo 로그인 회원 정보
     * @return 커서 기반 목록 응답
     */
    @ApiResponse(responseCode = "200", description = "모집글 목록을 조회했습니다.")
    @Operation(method = "GET", summary = "Dymit 스터디 모집글 목록 조회")
    fun getStudyRecruitmentList(
        cursor: String? = null,
        size: Int = 20,
        type: StudyRecruitmentRequestType = StudyRecruitmentRequestType.DYMIT,
        mine: Boolean = false,
        memberInfo: MemberInfo? = null
    ): ListResponse<DymitStudyRecruitmentSummaryResponse>

    /**
     * Dymit 스터디 모집글을 단건 조회합니다.
     *
     * @param recruitmentId 모집글 식별자
     * @return 모집글 응답
     */
    @ApiResponse(responseCode = "200", description = "모집글을 조회했습니다.")
    @Operation(method = "GET", summary = "Dymit 스터디 모집글 단건 조회")
    fun getStudyRecruitment(recruitmentId: String): DymitStudyRecruitmentResponse

    /**
     * Dymit 스터디 모집글을 수정합니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param recruitmentId 모집글 식별자
     * @param request 모집글 수정 요청
     * @return 수정된 모집글 응답
     */
    @ApiResponse(responseCode = "200", description = "모집글을 수정했습니다.")
    @Operation(method = "PUT", summary = "Dymit 스터디 모집글 수정")
    @SecurityRequirement(name = "bearer-jwt")
    fun updateStudyRecruitment(
        memberInfo: MemberInfo,
        recruitmentId: String,
        @Valid request: UpdateStudyRecruitmentRequest
    ): DymitStudyRecruitmentResponse

    /**
     * Dymit 스터디 모집글을 삭제합니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param recruitmentId 모집글 식별자
     */
    @ApiResponse(responseCode = "204", description = "모집글을 삭제했습니다.")
    @Operation(method = "DELETE", summary = "Dymit 스터디 모집글 삭제")
    @SecurityRequirement(name = "bearer-jwt")
    fun deleteStudyRecruitment(
        memberInfo: MemberInfo,
        recruitmentId: String
    )
}
