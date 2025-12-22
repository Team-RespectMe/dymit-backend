package net.noti_me.dymit.dymit_backend_api.application.member.usecases

interface ForceDeleteMemberUseCase {

    fun forceDelete(memberId: String)
}