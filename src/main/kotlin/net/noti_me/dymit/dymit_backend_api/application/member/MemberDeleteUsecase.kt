package net.noti_me.dymit.dymit_backend_api.application.member.usecases

interface MemberDeleteUsecase {

    fun deleteMember(requesterId: String, memberId: String)
}
