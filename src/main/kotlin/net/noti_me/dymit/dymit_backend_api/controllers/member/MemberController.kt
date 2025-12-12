package net.noti_me.dymit.dymit_backend_api.controllers

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.ManageDeviceTokenUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.ChangeMemberImageUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.QueryMemberUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.CreateMemberUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.DeleteMemberUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.ChangeNicknameUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.UpdateInterestsUseCase
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.web.bind.annotation.*
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberProfileResponse
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberNicknameUpdateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.MemberApi
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.DeviceTokenCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateResponse
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.ProfileImageUploadRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.UpdateInterestsRequest
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated

@RestController
@Validated
//@RequestMapping("/api/v1/members")
class MemberController(
    private val memberCreateUsecase: CreateMemberUseCase,
    private val memberQueryUsecase: QueryMemberUseCase,
    private val memberDeleteUsecase: DeleteMemberUseCase,
    private val memberUpdateNicknameUsecase: ChangeNicknameUseCase,
    private val memberImageUploadUsecase: ChangeMemberImageUseCase,
    private val updateInterestsUseCase: UpdateInterestsUseCase,
    private val deviceTokenUsecase: ManageDeviceTokenUseCase
) : MemberApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getMemberProfile(
        loginMember: MemberInfo,
        memberId: String
    ): MemberProfileResponse {
        return MemberProfileResponse.from(
            memberQueryUsecase.getMemberById(loginMember, memberId)
        )
    }

    override fun patchNickname(
        loginMember: MemberInfo,
        memberId: String,
        @Valid @Sanitize request: MemberNicknameUpdateRequest)
    : MemberProfileResponse {
        val memberDto = memberUpdateNicknameUsecase.updateNickname(
            loginMember,
            memberId,
            request.toCommand()
        )
        return MemberProfileResponse.from(memberDto)
    }

    override fun patchInterests(
        loginMember: MemberInfo,
        memberId: String,
        @Valid @Sanitize request: UpdateInterestsRequest
    ): MemberProfileResponse {
        return MemberProfileResponse.from( updateInterestsUseCase.updateInterests(
            loginMember = loginMember,
            command = request.toCommand()
        ))
    }

//    @PostMapping
    override fun createMember(
        @Valid @Sanitize request: MemberCreateRequest
    ): MemberCreateResponse {
        val result = memberCreateUsecase.createMember(
            request.toCommand()
        )
        return MemberCreateResponse.from(result)
    }

    override fun checkNickname(nickname: String) {
        logger.debug("checkNickname called with nickname: $nickname")
        return memberCreateUsecase.checkNickname(nickname)
    }

    override fun uploadProfileImage(
        loginMember: MemberInfo,
        memberId: String,
        @Valid @Sanitize request: ProfileImageUploadRequest
    ): MemberProfileResponse {
        return MemberProfileResponse.from(
            memberImageUploadUsecase.changeProfileImage(
                loginMember = loginMember,
                command = request.toCommand(memberId)
            )
        )
    }

    override fun deleteMember(loginMember: MemberInfo, memberId: String) {
        return memberDeleteUsecase.deleteMember(
            loginMember = loginMember,
            memberId = memberId
        )
    }

    override fun registerDeviceToken(loginMember: MemberInfo, memberId: String, request: DeviceTokenCommandRequest) {
        deviceTokenUsecase.registerDeviceToken(
            member = loginMember,
            deviceToken = request.deviceToken
        )
    }

    override fun unregisterDeviceToken(loginMember: MemberInfo, memberId: String, request: DeviceTokenCommandRequest) {
        deviceTokenUsecase.unregisterDeviceToken(
            member = loginMember,
            deviceToken = request.deviceToken
        )
    }
}

