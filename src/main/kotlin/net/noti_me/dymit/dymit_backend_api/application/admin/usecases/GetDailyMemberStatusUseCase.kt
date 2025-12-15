package net.noti_me.dymit.dymit_backend_api.application.admin.usecases

import net.noti_me.dymit.dymit_backend_api.application.admin.dto.DailyMemberStatusDto
import java.time.LocalDateTime

interface GetDailyMemberStatusUseCase {

    fun getStatusBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<DailyMemberStatusDto>
}