package net.noti_me.dymit.dymit_backend_api.reminder.application.usecase

import org.quartz.Job
import org.quartz.JobExecutionContext

/**
 * 역할 배정 알림 배치를 위한 예약 클래스입니다.
 */
class RoleAsssignedReminderJob : Job {

    /**
     * 예약된 역할 배정 알림 작업을 실행합니다.
     */
    override fun execute(context: JobExecutionContext) {
        // TODO: Implement role assigned reminder job
    }
}
