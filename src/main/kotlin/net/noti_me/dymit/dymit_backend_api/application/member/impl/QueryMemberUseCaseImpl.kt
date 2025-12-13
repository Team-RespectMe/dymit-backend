package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.usecases.QueryMemberUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberQueryDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import org.springframework.stereotype.Service

@Service
class QueryMemberUseCaseImpl(
    private val loadMemberPort: LoadMemberPort
): QueryMemberUseCase {

    override fun getMemberById(loginMember: MemberInfo, memberId: String): MemberQueryDto {
        val member = loadMemberPort.loadById(memberId)
            ?: throw NotFoundException(message="존재하지 않는 멤버입니다.")

        if (loginMember.memberId != memberId) {
            throw ForbiddenException(message="다른 멤버의 프로필을 조회할 수 없습니다.")
        }

        return MemberQueryDto.fromEntity(member)
    }
}