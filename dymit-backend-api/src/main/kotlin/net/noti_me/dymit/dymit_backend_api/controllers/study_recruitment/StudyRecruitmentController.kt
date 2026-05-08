package net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment

import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.application.study_recruitment.StudyRecruitmentServiceFacade
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment.dto.QueryStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment.dto.StudyRecruitmentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * StudyRecruitmentApi 구현체입니다.
 *
 * 스터디 모집 목록 조회 요청을 수신하고 서비스 파사드로 위임합니다.
 */
@RestController
class StudyRecruitmentController(
    private val studyRecruitmentServiceFacade: StudyRecruitmentServiceFacade
) : StudyRecruitmentApi {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getStudyRecruitments(
        @LoginMember memberInfo: MemberInfo,
        @RequestParam(required=false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int
    ): ListResponse<StudyRecruitmentResponse> {
        val query = QueryStudyRecruitmentRequest(
            cursor = cursor,
            size = size
        ).toQuery()
        val recruitments = studyRecruitmentServiceFacade.getStudyRecruitments(query)
        val responses = recruitments.map { StudyRecruitmentResponse.from(it) }

        return ListResponse.of(
            size = query.size,
            items = responses,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { query.size }
            }
        )
    }
}
