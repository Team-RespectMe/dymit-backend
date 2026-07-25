package net.noti_me.dymit.dymit_backend_api.member.application.impl

import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateInterestsCommand
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.UpdateInterestsUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.SaveMemberPort
import org.springframework.stereotype.Service

@Service
class UpdateInterestsUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort
): UpdateInterestsUseCase {

    override fun updateInterests(
        loginMember: MemberInfo,
        command: UpdateInterestsCommand
    ): MemberDto {
        val member = loadMemberPort.loadById(loginMember.memberId)
            ?: throw NotFoundException(message = "존재하지 않는 멤버입니다.")
        member.updateInterests(command.interests.toMutableSet())
        val updatedMember = saveMemberPort.update(member)
        return MemberDto.fromEntity(updatedMember)
    }
}