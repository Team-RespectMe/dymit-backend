package net.noti_me.dymit.dymit_backend_api.controllers

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.member.MemberServiceFacade
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.*
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.member.MemberApi
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.annotation.security.PermitAll

@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberServiceFacade: MemberServiceFacade
) : MemberApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/{memberId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getMemberProfile(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String
    ): MemberProfileResponse {
        return MemberProfileResponse.from(
            memberServiceFacade.getMember(loginMember, memberId)
        )
    }

    @PatchMapping("/{memberId}/nickname")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun patchNickname(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @RequestBody @Valid @Sanitize request: MemberNicknameUpdateRequest)
    : MemberProfileResponse {
        return MemberProfileResponse.from(memberServiceFacade.changeNickname(
            loginMember = loginMember,
            memberId = memberId,
            command = request.toCommand()
        ))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PermitAll
    override fun createMember(
        @RequestBody @Valid @Sanitize request: MemberCreateRequest
    ): MemberCreateResponse {
        return MemberCreateResponse.from( memberServiceFacade.createMember(request.toCommand()) )
    }

    @GetMapping("/nickname-validation")
    @ResponseStatus(HttpStatus.OK)
    @PermitAll
    override fun checkNickname(
        @RequestParam nickname: String
    ) {
        return memberServiceFacade.checkNicknameAvailability(nickname)
    }

    @PutMapping("/{memberId}/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun uploadProfileImage(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @ModelAttribute @Valid @Sanitize request: ProfileImageUploadRequest
    ): MemberProfileResponse {
        return MemberProfileResponse.from(
            memberServiceFacade.changeMemberImage(
                loginMember = loginMember,
                command = request.toCommand(memberId)
            )
        )
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun deleteMember(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String
    ) {
        return memberServiceFacade.deleteMember(
            loginMember = loginMember,
            memberId = memberId
        )
    }

    @PostMapping("/{memberId}/device-tokens")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun registerDeviceToken(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @RequestBody @Valid @Sanitize request: DeviceTokenCommandRequest
    ) {
        memberServiceFacade.registerDeviceToken(
            loginMember = loginMember,
            deviceToken = request.deviceToken
        )
    }

    @DeleteMapping("/{memberId}/device-tokens" )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun unregisterDeviceToken(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @RequestBody @Valid @Sanitize request: DeviceTokenCommandRequest
    ) {
        memberServiceFacade.unregisterDeviceToken(
            loginMember = loginMember,
            deviceToken = request.deviceToken
        )
    }

    @PatchMapping("/{memberId}/interests")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun patchInterests(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @RequestBody @Valid @Sanitize request: UpdateInterestsRequest
    ): MemberProfileResponse {
        return MemberProfileResponse.from(
            memberServiceFacade.updateInterests(
                loginMember = loginMember,
                command = request.toCommand()
            )
        )
    }
}

