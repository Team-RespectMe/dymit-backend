package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.usecases.ManageDeviceTokenUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.springframework.stereotype.Service

@Service
class ManageDeviceTokenUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort
) : ManageDeviceTokenUseCase {

    override fun registerDeviceToken(
        member: MemberInfo,
        deviceToken: String
    ) {
        val tokenOwners = loadMemberPort.loadByDeviceToken(deviceToken)
        val memberEntity = loadMemberPort.loadById(member.memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")
        
        val targetDeviceToken = DeviceToken(token=deviceToken, isActive=true)

        tokenOwners.forEach { owner -> 
            owner.removeDeviceToken(targetDeviceToken)
            saveMemberPort.update(owner)
        }

        memberEntity.addDeviceToken(
            DeviceToken(
                token = deviceToken,
                isActive = true
            )
        )

        saveMemberPort.update(memberEntity)
    }

    override fun unregisterDeviceToken(
        member: MemberInfo,
        deviceToken: String
    ) {
        val memberEntity = loadMemberPort.loadById(member.memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")

        val token = DeviceToken(token=deviceToken, isActive=true)
        memberEntity.removeDeviceToken(token)

        saveMemberPort.update(memberEntity)
    }
}
