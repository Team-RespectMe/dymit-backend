package net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeSummaryDto

interface GetNoticesUseCase {

    fun getNotices(cursor: String?, size: Int): List<ServerNoticeSummaryDto>
}