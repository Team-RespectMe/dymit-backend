package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.usecases.CheckNicknameUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import org.springframework.stereotype.Service

@Service
class CheckNicknameUseCaseImpl(
    private val loadMemberPort: LoadMemberPort
): CheckNicknameUseCase {

    override fun isNicknameAvailable(nickname: String) {
        if ( loadMemberPort.existsByNickname(nickname) ) {
            throw ConflictException(code = "CONFLICT", message = "이미 사용 중인 닉네임입니다.")
        }
    }
}