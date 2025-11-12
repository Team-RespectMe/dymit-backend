package net.noti_me.dymit.dymit_backend_api.configs

import net.noti_me.dymit.dymit_backend_api.application.batch.DailyScheduleNotificationJob
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

@Configuration
class QuartzConfig {

    // TODO Discord Logger를 설정하여 배치 작업 실패시 알림을 받을 수 있도록 설정할 것

    @Bean 
    fun dailyScheduleNotificationJobDetail(): JobDetail {
        return JobBuilder.newJob(DailyScheduleNotificationJob::class.java) 
            .withIdentity("dailyScheduleNotificationJob")
            .storeDurably()
            .build()
    }


    @Bean
    fun triggerOn9AMUTC9(jobDetail: JobDetail): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("dailyScheduleNotificationTrigger")
            .withSchedule(
                CronScheduleBuilder.cronSchedule("0 0 9 * * ?") // 매일 오전 9시 (UTC+9 기준)
                    .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"))
            )
            .build()
    }
}

