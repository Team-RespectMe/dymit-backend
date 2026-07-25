package net.noti_me.dymit.dymit_backend_api.member.application.impl

import net.noti_me.dymit.dymit_backend_api.member.application.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.member.application.dto.UpdateNicknameCommand
import net.noti_me.dymit.dymit_backend_api.member.application.usecases.ChangeNicknameUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.SaveMemberPort
import org.springframework.stereotype.Service

@Service
class ChangeNicknameUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort
): ChangeNicknameUseCase {

    override fun updateNickname(
        loginMember: MemberInfo,
        memberId: String,
        command: UpdateNicknameCommand
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