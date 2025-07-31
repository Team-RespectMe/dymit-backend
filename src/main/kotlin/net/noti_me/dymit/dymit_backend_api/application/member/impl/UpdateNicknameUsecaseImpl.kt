package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberNicknameUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.UpdateNicknameUsecase
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.springframework.stereotype.Service

@Service
class UpdateNicknameUsecaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort
): UpdateNicknameUsecase {

    override fun updateNickname(
        loginMember: MemberInfo,
        memberId: String,
        command: MemberNicknameUpdateCommand
    ): MemberDto {
        val member = loadMemberPort.loadById(memberId)
            ?: throw NotFoundException(message = "존재하지 않는 회원입니다.")

        if (loadMemberPort.existsByNickname(command.nickname)) {
            throw ConflictException(message="이미 사용 중인 닉네임입니다.")
        }

        if ( loginMember.memberId != member.identifier ) {
            throw ForbiddenException(message = "다른 사용자의 닉네임을 변경할 수 없습니다.")
        }

        member.changeNickname(command.nickname)
        val updatedMember = saveMemberPort.update(member)
        return MemberDto.fromEntity(updatedMember)
    }
}