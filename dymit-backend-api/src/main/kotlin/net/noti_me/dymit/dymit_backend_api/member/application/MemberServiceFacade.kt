package net.noti_me.dymit.dymit_backend_api.member.application

import net.noti_me.dymit.dymit_backend_api.member.application.dto.CreateMemberCommand
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateInterestsCommand
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateMemberProfileImageCommand
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateNicknameCommand
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.ChangeMemberImageUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.ChangeNicknameUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.CheckNicknameUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.CreateMemberUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.DeleteMemberUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.ManageDeviceTokenUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.QueryMemberUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.UpdateInterestsUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

@Service
class MemberServiceFacade(
    private val createMemberUseCase: CreateMemberUseCase,
    private val changeNicknameUseCase: ChangeNicknameUseCase,
    private val changeMemberImageUseCase: ChangeMemberImageUseCase,
    private val updateInterestsUseCase: UpdateInterestsUseCase,
    private val manageDeviceTokenUseCase: ManageDeviceTokenUseCase,
    private val deleteMemberUseCase: DeleteMemberUseCase,
    private val queryMemberUseCase: QueryMemberUseCase,
    private val checkNicknameUseCase: CheckNicknameUseCase,
) {

    fun createMember(command: CreateMemberCommand) =
        createMemberUseCase.createMember(command)

    fun changeNickname(loginMember: MemberInfo, memberId: String, command: UpdateNicknameCommand) =
        changeNicknameUseCase.updateNickname(
            loginMember = loginMember,
            memberId = memberId,
            command = command
        )

    fun changeMemberImage(loginMember: MemberInfo, command: UpdateMemberProfileImageCommand) =
        changeMemberImageUseCase.changeProfileImage(loginMember, command)

    fun updateInterests(loginMember: MemberInfo, command: UpdateInterestsCommand) =
        updateInterestsUseCase.updateInterests(loginMember, command)

    fun registerDeviceToken(loginMember: MemberInfo, deviceToken: String) =
        manageDeviceTokenUseCase.registerDeviceToken(loginMember, deviceToken)

    fun unregisterDeviceToken(loginMember: MemberInfo, deviceToken: String) =
        manageDeviceTokenUseCase.unregisterDeviceToken(loginMember, deviceToken)

    fun deleteMember(loginMember: MemberInfo, memberId: String) =
        deleteMemberUseCase.deleteMember(loginMember, memberId)

    fun getMember(loginMember: MemberInfo, memberId: String) =
        queryMemberUseCase.getMemberById(loginMember, memberId)

    fun checkNicknameAvailability(nickname: String) =
        checkNicknameUseCase.isNicknameAvailable(nickname)
}