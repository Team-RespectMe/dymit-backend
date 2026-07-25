package net.noti_me.dymit.dymit_backend_api.member.application.usecases

interface ForceDeleteMemberUseCase {

    fun forceDelete(memberId: String)
}