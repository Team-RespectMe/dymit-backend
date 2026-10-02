package net.noti_me.dymit.dymit_backend_api.configs

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsReportJob
import net.noti_me.dymit.dymit_backend_api.member.adapter.`in`.daily_statistics.MemberDailyStatisticsJob
import net.noti_me.dymit.dymit_backend_api.reminder.application.usecase.DailyScheduleReminderJob
import net.noti_me.dymit.dymit_backend_api.reminder.application.usecase.HourlyScheduleReminderJob
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.`in`.daily_statistics.StudyGroupDailyStatisticsJob
import net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.`in`.daily_statistics.StudyScheduleDailyStatisticsJob
import net.noti_me.dymit.dymit_backend_api.task.adapter.`in`.daily_statistics.TaskDailyStatisticsJob
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

/**
 * Registers Quartz jobs and their stable schedules.
 */
@Configuration
class QuartzConfig {

    /**
     * Registers the member daily-statistics job.
     */
    @Bean
    fun memberDailyStatisticsJobDetail(): JobDetail {
        return JobBuilder.newJob(MemberDailyStatisticsJob::class.java)
            .withIdentity("memberDailyStatisticsJob")
            .storeDurably()
            .build()
    }

    /**
     * Registers the study-group daily-statistics job.
     */
    @Bean
    fun studyGroupDailyStatisticsJobDetail(): JobDetail {
        return JobBuilder.newJob(StudyGroupDailyStatisticsJob::class.java)
            .withIdentity("studyGroupDailyStatisticsJob")
            .storeDurably()
            .build()
    }

    /**
     * Registers the study-schedule daily-statistics job.
     */
    @Bean
    fun studyScheduleDailyStatisticsJobDetail(): JobDetail {
        return JobBuilder.newJob(StudyScheduleDailyStatisticsJob::class.java)
            .withIdentity("studyScheduleDailyStatisticsJob")
            .storeDurably()
            .build()
    }

    /**
     * Registers the task daily-statistics job.
     */
    @Bean
    fun taskDailyStatisticsJobDetail(): JobDetail {
        return JobBuilder.newJob(TaskDailyStatisticsJob::class.java)
            .withIdentity("taskDailyStatisticsJob")
            .storeDurably()
            .build()
    }

    /**
     * Registers the completed daily-statistics report job.
     */
    @Bean
    fun dailyStatisticsReportJobDetail(): JobDetail {
        return JobBuilder.newJob(DailyStatisticsReportJob::class.java)
            .withIdentity("dailyStatisticsReportJob")
            .storeDurably()
            .build()
    }

    /**
     * Registers the daily schedule-reminder job.
     */
    @Bean
    fun dailyScheduleReminderJobDetail(): JobDetail {
        return JobBuilder.newJob(DailyScheduleReminderJob::class.java) 
            .withIdentity("dailyScheduleReminderJob")
            .storeDurably()
            .build()
    }

    /**
     * Registers the hourly schedule-reminder job.
     */
    @Bean
    fun hourlyScheduleReminderJobDetail(): JobDetail {
        return JobBuilder.newJob(HourlyScheduleReminderJob::class.java)
            .withIdentity("hourlyScheduleReminderJob")
            .storeDurably()
            .build()
    }

    /**
     * Runs member statistics every day at 04:00 Asia/Seoul.
     */
    @Bean
    fun memberDailyStatisticsTrigger(
        @Qualifier("memberDailyStatisticsJobDetail") jobDetail: JobDetail
    ): Trigger {
        return dailyStatisticsTrigger(jobDetail, "memberDailyStatisticsTrigger")
    }

    /**
     * Runs study-group statistics every day at 04:00 Asia/Seoul.
     */
    @Bean
    fun studyGroupDailyStatisticsTrigger(
        @Qualifier("studyGroupDailyStatisticsJobDetail") jobDetail: JobDetail
    ): Trigger {
        return dailyStatisticsTrigger(jobDetail, "studyGroupDailyStatisticsTrigger")
    }

    /**
     * Runs study-schedule statistics every day at 04:00 Asia/Seoul.
     */
    @Bean
    fun studyScheduleDailyStatisticsTrigger(
        @Qualifier("studyScheduleDailyStatisticsJobDetail") jobDetail: JobDetail
    ): Trigger {
        return dailyStatisticsTrigger(jobDetail, "studyScheduleDailyStatisticsTrigger")
    }

    /**
     * Runs task statistics every day at 04:00 Asia/Seoul.
     */
    @Bean
    fun taskDailyStatisticsTrigger(
        @Qualifier("taskDailyStatisticsJobDetail") jobDetail: JobDetail
    ): Trigger {
        return dailyStatisticsTrigger(jobDetail, "taskDailyStatisticsTrigger")
    }

    /**
     * Runs the completed daily-statistics report every day at 05:00 Asia/Seoul.
     */
    @Bean
    fun dailyStatisticsReportTrigger(
        @Qualifier("dailyStatisticsReportJobDetail") jobDetail: JobDetail
    ): Trigger {
        return dailyStatisticsTrigger(
            jobDetail,
            "dailyStatisticsReportTrigger",
            DAILY_STATISTICS_REPORT_CRON
        )
    }

    /**
     * Runs the daily schedule reminder at 09:00 Asia/Seoul.
     */
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

    /**
     * Runs the hourly schedule reminder in the Asia/Seoul time zone.
     */
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

    private fun dailyStatisticsTrigger(
        jobDetail: JobDetail,
        identity: String,
        cron: String = DAILY_STATISTICS_CRON
    ): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity(identity)
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cron)
                    .inTimeZone(TimeZone.getTimeZone(DAILY_STATISTICS_TIME_ZONE))
            )
            .build()
    }

    private companion object {
        const val DAILY_STATISTICS_CRON = "0 0 4 * * ?"
        const val DAILY_STATISTICS_REPORT_CRON = "0 0 5 * * ?"
        const val DAILY_STATISTICS_TIME_ZONE = "Asia/Seoul"
    }
}
