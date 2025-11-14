package net.noti_me.dymit.dymit_backend_api.configs

import net.noti_me.dymit.dymit_backend_api.application.reminder.DailyScheduleReminderJob
import net.noti_me.dymit.dymit_backend_api.application.reminder.HourlyScheduleReminderJob
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

@Configuration
class QuartzConfig {

    @Bean
    fun dailyScheduleReminderJobDetail(): JobDetail {
        return JobBuilder.newJob(DailyScheduleReminderJob::class.java) 
            .withIdentity("dailyScheduleReminderJob")
            .storeDurably()
            .build()
    }

    @Bean
    fun hourlyScheduleReminderJobDetail(): JobDetail {
        return JobBuilder.newJob(HourlyScheduleReminderJob::class.java)
            .withIdentity("hourlyScheduleReminderJob")
            .storeDurably()
            .build()
    }

    @Bean
    fun triggerOn9AMUTC9(
        @Qualifier("dailyScheduleReminderJobDetail") jobDetail: JobDetail
    ): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("dailyScheduleReminderTrigger")
            .withSchedule(
                CronScheduleBuilder.cronSchedule("0 0 9 * * ?")
                    .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"))
            )
            .build()
    }

    @Bean
    fun triggerEveryHour(
        @Qualifier("hourlyScheduleReminderJobDetail") jobDetail: JobDetail
    ): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("hourlyScheduleReminderTrigger")
            .withSchedule(
                CronScheduleBuilder.cronSchedule("0 0 * * * ?")
                    .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"))
            )
            .build()
    }
}

