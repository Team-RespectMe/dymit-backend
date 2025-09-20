package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.IntegerValueResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "MyStudyGroup", description = "내 소유 스터디 그룹 관련 API")
interface MyStudyGroupApi {

    /**
     * 현재 로그인 한 사용자가 그룹 소유자인 그룹의 수를 반환합니다.
     * @return OwnedStudyGroupCountResponse
     */
    @GetMapping("/members/me/study-groups/owned/count")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "내가 소유한 스터디 그룹 수 조회 API", description = "현재 로그인한 사용자가 소유한 스터디 그룹의 수를 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공")
    ])
    fun countMyOwnedStudyGroups(
        @LoginMember memberInfo: MemberInfo
    ): IntegerValueResponse
}