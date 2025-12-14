package net.noti_me.dymit.dymit_backend_api.application.member.usecases

interface CheckNicknameUseCase {

    fun isNicknameAvailable(nickname: String): Unit
}