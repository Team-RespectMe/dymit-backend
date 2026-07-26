package net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase

import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.ServerNoticeDto

interface GetNoticeUseCase {

    fun execute(noticeId: String): ServerNoticeDto
}
