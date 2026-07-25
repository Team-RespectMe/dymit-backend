package net.noti_me.dymit.dymit_backend_api.member.application.usecases

interface CheckNicknameUseCase {

    fun isNicknameAvailable(nickname: String): Unit
}