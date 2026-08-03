package net.noti_me.dymit.dymit_backend_api.units.common.daily_statistics

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.member.application.MemberDailyStatisticsService
import net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.daily_statistics.CollectMemberDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.daily_statistics.MemberDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.daily_statistics.MemberDailyStatisticsPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.StudyGroupDailyStatisticsService
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.daily_statistics.CollectStudyGroupDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`out`.daily_statistics.StudyGroupDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`out`.daily_statistics.StudyGroupDailyStatisticsPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.StudyScheduleDailyStatisticsService
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.daily_statistics.CollectStudyScheduleDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`out`.daily_statistics.StudyScheduleDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`out`.daily_statistics.StudyScheduleDailyStatisticsPort
import net.noti_me.dymit.dymit_backend_api.task.application.TaskDailyStatisticsService
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.daily_statistics.CollectTaskDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.daily_statistics.TaskDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.daily_statistics.TaskDailyStatisticsPort
import java.time.LocalDate

internal class DailyStatisticsServiceTest : BehaviorSpec({
    val date = LocalDate.of(2026, 7, 28)
    val start = date.atTime(4, 0)
    val end = date.plusDays(1).atTime(4, 0)

    Given("daily-statistics collectors") {
        val memberPort = mockk<MemberDailyStatisticsPort>()
        val groupPort = mockk<StudyGroupDailyStatisticsPort>()
        val schedulePort = mockk<StudyScheduleDailyStatisticsPort>()
        val taskPort = mockk<TaskDailyStatisticsPort>()
        val memberStatistics = MemberDailyStatisticsDto(2, 1, 4)
        val groupStatistics = StudyGroupDailyStatisticsDto(3)
        val scheduleStatistics = StudyScheduleDailyStatisticsDto(7, 5)
        val taskStatistics = TaskDailyStatisticsDto(11, 13)
        every { memberPort.collect(start, end) } returns memberStatistics
        every { memberPort.upsert(date, start, end, memberStatistics) } returns true
        every { groupPort.collect(start, end) } returns groupStatistics
        every { groupPort.upsert(date, start, end, groupStatistics) } returns false
        every { schedulePort.collect(start, end) } returns scheduleStatistics
        every { schedulePort.upsert(date, start, end, scheduleStatistics) } returns true
        every { taskPort.collect(start, end) } returns taskStatistics
        every { taskPort.upsert(date, start, end, taskStatistics) } returns false

        When("each service collects and writes its owned metrics") {
            MemberDailyStatisticsService(memberPort)
                .execute(CollectMemberDailyStatisticsCommand(date, start, end))
            StudyGroupDailyStatisticsService(groupPort)
                .execute(CollectStudyGroupDailyStatisticsCommand(date, start, end))
            StudyScheduleDailyStatisticsService(schedulePort)
                .execute(CollectStudyScheduleDailyStatisticsCommand(date, start, end))
            TaskDailyStatisticsService(taskPort)
                .execute(CollectTaskDailyStatisticsCommand(date, start, end))

            Then("they forward metrics to only their ports without any notifier dependency") {
                verify(exactly = 1) { memberPort.upsert(date, start, end, memberStatistics) }
                verify(exactly = 1) { groupPort.upsert(date, start, end, groupStatistics) }
                verify(exactly = 1) { schedulePort.upsert(date, start, end, scheduleStatistics) }
                verify(exactly = 1) { taskPort.upsert(date, start, end, taskStatistics) }
            }
        }
    }

})
