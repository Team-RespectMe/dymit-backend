package net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface DeleteNoticeUseCase {

    fun delete(loginMember: MemberInfo, noticeId: String): Unit
}