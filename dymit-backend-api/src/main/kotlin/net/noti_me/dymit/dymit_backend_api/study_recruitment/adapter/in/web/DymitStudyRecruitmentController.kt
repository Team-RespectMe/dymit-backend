package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.web

import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CreateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.DeleteDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentListUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.UpdateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DeleteDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentListQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.DymitStudyRecruitmentApi
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.CreateStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentSummaryResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.UpdateStudyRecruitmentRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Dymit 스터디 모집글 v2 웹 컨트롤러입니다.
 *
 * @property createUseCase 모집글 생성 유즈케이스
 * @property getListUseCase 모집글 목록 조회 유즈케이스
 * @property getUseCase 모집글 단건 조회 유즈케이스
 * @property updateUseCase 모집글 수정 유즈케이스
 * @property deleteUseCase 모집글 삭제 유즈케이스
 */
@RestController
class DymitStudyRecruitmentController(
    private val createUseCase: CreateDymitStudyRecruitmentUseCase,
    private val getListUseCase: GetDymitStudyRecruitmentListUseCase,
    private val getUseCase: GetDymitStudyRecruitmentUseCase,
    private val updateUseCase: UpdateDymitStudyRecruitmentUseCase,
    private val deleteUseCase: DeleteDymitStudyRecruitmentUseCase
) : DymitStudyRecruitmentApi {

    /**
     * 그룹 소유자의 Dymit 모집글 생성 요청을 처리합니다.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun createStudyRecruitment(
        @LoginMember memberInfo: MemberInfo,
        @RequestBody @Valid @Sanitize request: CreateStudyRecruitmentRequest
    ): DymitStudyRecruitmentResponse {
        return DymitStudyRecruitmentResponse.from(
            createUseCase.execute(memberInfo, request.toCommand())
        )
    }

    /**
     * Dymit 모집글 목록 조회 요청을 처리합니다.
     */
    @GetMapping
    @PermitAll
    override fun getStudyRecruitmentList(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int
    ): ListResponse<DymitStudyRecruitmentSummaryResponse> {
        val query = GetDymitStudyRecruitmentListQuery(cursor = cursor, size = size)
        val responses = getListUseCase.execute(query)
            .map(DymitStudyRecruitmentSummaryResponse::from)

        return ListResponse.of(
            size = query.size,
            items = responses,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { query.size }
            }
        )
    }

    /**
     * Dymit 모집글 단건 조회 요청을 처리합니다.
     */
    @GetMapping("/{recruitmentId}")
    @PermitAll
    override fun getStudyRecruitment(
        @PathVariable recruitmentId: String
    ): DymitStudyRecruitmentResponse {
        val query = GetDymitStudyRecruitmentQuery(recruitmentId)
        return DymitStudyRecruitmentResponse.from(getUseCase.execute(query))
    }

    /**
     * 그룹 소유자의 Dymit 모집글 수정 요청을 처리합니다.
     */
    @PutMapping("/{recruitmentId}")
    @RolesAllowed("MEMBER", "ADMIN")
    override fun updateStudyRecruitment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable recruitmentId: String,
        @RequestBody @Valid @Sanitize request: UpdateStudyRecruitmentRequest
    ): DymitStudyRecruitmentResponse {
        return DymitStudyRecruitmentResponse.from(
            updateUseCase.execute(memberInfo, request.toCommand(recruitmentId))
        )
    }

    /**
     * 그룹 소유자의 Dymit 모집글 삭제 요청을 처리합니다.
     */
    @DeleteMapping("/{recruitmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun deleteStudyRecruitment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable recruitmentId: String
    ) {
        deleteUseCase.execute(
            memberInfo = memberInfo,
            command = DeleteDymitStudyRecruitmentCommand(recruitmentId)
        )
    }
}
