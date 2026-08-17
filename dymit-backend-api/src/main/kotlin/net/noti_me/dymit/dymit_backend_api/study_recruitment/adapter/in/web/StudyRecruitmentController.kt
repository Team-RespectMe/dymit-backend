package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.web

import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.QueryStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.StudyRecruitmentApi
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.QueryStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.StudyRecruitmentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 스터디 모집 목록 조회 웹 어댑터입니다.
 *
 * @property queryStudyRecruitmentUseCase 스터디 모집 목록 조회 유즈케이스
 */
@RestController
class StudyRecruitmentController(
    private val queryStudyRecruitmentUseCase: QueryStudyRecruitmentUseCase
) : StudyRecruitmentApi {

    /**
     * 로그인 사용자를 위한 스터디 모집 목록을 조회합니다.
     *
     * @param memberInfo 로그인 사용자 정보
     * @param cursor 다음 페이지 조회를 위한 커서
     * @param size 조회 개수
     * @return 커서 기반 목록 응답
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getStudyRecruitments(
        @LoginMember memberInfo: MemberInfo,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int
    ): ListResponse<StudyRecruitmentResponse> {
        val command = QueryStudyRecruitmentRequest(
            cursor = cursor,
            size = size
        ).toCommand()
        val responses = queryStudyRecruitmentUseCase.execute(command)
            .map(StudyRecruitmentResponse::from)

        return ListResponse.of(
            size = command.size,
            items = responses,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { command.size }
            }
        )
    }
}
