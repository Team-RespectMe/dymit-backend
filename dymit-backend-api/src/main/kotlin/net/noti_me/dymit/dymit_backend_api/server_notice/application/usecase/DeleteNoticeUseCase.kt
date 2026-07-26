package net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface DeleteNoticeUseCase {

    fun execute(loginMember: MemberInfo, noticeId: String): Unit
}