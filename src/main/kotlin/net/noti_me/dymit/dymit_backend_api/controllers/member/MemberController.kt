package net.noti_me.dymit.dymit_backend_api.controllers

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.*
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
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
    private val memberCreateUsecase: MemberCreateUsecase,
    private val memberQueryUsecase: MemberQueryUsecase,
    private val memberDeleteUsecase: MemberDeleteUsecase,
    private val memberUpdateNicknameUsecase: UpdateNicknameUsecase,
    private val memberImageUploadUsecase: ChangeMemberImageUseCase,
    private val deviceTokenUsecase: MemberDeviceTokenUsecase
) : MemberApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/{memberId}")
    @ResponseStatus(HttpStatus.OK)
    override fun getMemberProfile(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String
    ): MemberProfileResponse {
        return MemberProfileResponse.from(
            memberQueryUsecase.getMemberById(loginMember, memberId)
        )
    }

    @PatchMapping("/{memberId}/nickname")
    @ResponseStatus(HttpStatus.OK)
    override fun patchNickname(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @RequestBody @Valid @Sanitize request: MemberNicknameUpdateRequest)
    : MemberProfileResponse {
        val memberDto = memberUpdateNicknameUsecase.updateNickname(
            loginMember,
            memberId,
            request.toCommand()
        )
        return MemberProfileResponse.from(memberDto)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createMember(
        @RequestBody @Valid @Sanitize request: MemberCreateRequest
    ): MemberCreateResponse {
        logger.debug("Creating member with request: $request")
        val result = memberCreateUsecase.createMember(
            request.toCommand()
        )
        return MemberCreateResponse.from(result)
    }

    @PermitAll
    @GetMapping("/nickname-validation")
    @ResponseStatus(HttpStatus.OK)
    override fun checkNickname(
        @RequestParam @Valid @Nickname nickname: String
    ) {
        logger.debug("checkNickname called with nickname: $nickname")
        return memberCreateUsecase.checkNickname(nickname)
    }

    @PutMapping("/{memberId}/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.OK)
    override fun uploadProfileImage(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @ModelAttribute @Valid @Sanitize request: ProfileImageUploadRequest
    ): MemberProfileResponse {
        return MemberProfileResponse.from(
            memberImageUploadUsecase.changeProfileImage(
                loginMember = loginMember,
                command = request.toCommand(memberId)
            )
        )
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteMember(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String
    ) {
        return memberDeleteUsecase.deleteMember(
            loginMember = loginMember,
            memberId = memberId
        )
    }

    @PostMapping("/{memberId}/device-tokens")
    @ResponseStatus(HttpStatus.CREATED)
    override fun registerDeviceToken(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @RequestBody @Valid request: DeviceTokenCommandRequest
    ) {
        deviceTokenUsecase.registerDeviceToken(
            member = loginMember,
            deviceToken = request.deviceToken
        )
    }

    @DeleteMapping("/{memberId}/device-tokens" )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun unregisterDeviceToken(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @RequestBody @Valid request: DeviceTokenCommandRequest
    ) {
        deviceTokenUsecase.unregisterDeviceToken(
            member = loginMember,
            deviceToken = request.deviceToken
        )
    }
}

