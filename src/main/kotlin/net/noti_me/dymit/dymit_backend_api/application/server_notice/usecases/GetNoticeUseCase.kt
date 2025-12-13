package net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto

interface GetNoticeUseCase {

    fun getNotice(noticeId: String): ServerNoticeDto
}