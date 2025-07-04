package net.noti_me.dymit.dymit_backend_api.controllers

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.member.MemberQueryUsecase
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.MemberCreateUsecase
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberProfileResponse
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberNicknameUpdateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.MemberApi
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateResponse
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartRequest

@RestController
//@RequestMapping("/api/v1/members")
class MemberController(
    private val memberCreateUsecase: MemberCreateUsecase,
    private val memberQueryUsecase: MemberQueryUsecase
) : MemberApi {

    private val logger = LoggerFactory.getLogger(javaClass)

//    @GetMapping("/{memberId}")
    override fun getMemberProfile(
        loginMember: MemberInfo,
        memberId: String
    ): MemberProfileResponse {
        return MemberProfileResponse.from(
            memberQueryUsecase.getMemberById(loginMember, memberId)
        )
    }

//    @PatchMapping("/{memberId}/nickname")
    override fun patchNickname(
        loginMember: MemberInfo,
        memberId: String,
        request: MemberNicknameUpdateRequest)
    : MemberProfileResponse {
        TODO("Implement nickname update logic")
    }

//    @PostMapping
    override fun createMember(request: MemberCreateRequest)
    : MemberCreateResponse {
        logger.debug("Creating member with request: $request")
        val result = memberCreateUsecase.createMember(
            request.toCommand()
        )
        return MemberCreateResponse.from(result)
    }
}

