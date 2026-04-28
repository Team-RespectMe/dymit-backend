package net.noti_me.dymit.dymit_backend_api.application.member.batch

import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordQuartzLogger
import net.noti_me.dymit.dymit_backend_api.domain.member.DailyMemberStatus
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.DailyMemberStatusRepository
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@DisallowConcurrentExecution
class DailyMemberStatusJob(
    private val loadMemberPort: LoadMemberPort,
    private val memberStatusRepository: DailyMemberStatusRepository,
    private val discordLogger: DiscordQuartzLogger
): Job {

    /**
     * 매일 자정에 멤버 통계 정보를 집계하여 저장하는 배치 작업
     * - totalMembers: 전체 멤버 수
     * - newMembers: 오늘 가입한 멤버 수
     * - activeMembers: 오늘 활동한 멤버 수
     * - createdAt: 통계가 기록된 시각
     * 한국 시간 오전 4시에 실행되어야 함.
     * 이전 일 오전 4시부터 현재 일 오전 3시 59분 59초까지의 데이터를 집계
     */
    override fun execute(context: JobExecutionContext?) {
        val now = LocalDateTime.now()
        // 마감 시각은 한국 시간 오전 4시, 서버는 UTC 기준이므로 -9시간 즉 19시로 설정
        val endOfDay = now.withHour(19).withMinute(0).withSecond(0).withNano(0)
        val startOfDay = endOfDay.minusDays(1)

        val totalMembersCount = loadMemberPort.countAll()
        val newMembersCount = loadMemberPort.countByCreatedAtBetween(
            startOfDay,
            endOfDay
        )
        val activeMembersCount = loadMemberPort.countByLastAccessedAtBetween(
            startOfDay,
            endOfDay
        )
        val leaveMembersCount = loadMemberPort.countByLastAccessedAtBetween(
            startOfDay,
            endOfDay,
            isDeleted = true
        )

        val memberStatus = DailyMemberStatus(
            newMemberCount = newMembersCount,
            activeMemberCount = activeMembersCount,
            totalMemberCount = totalMembersCount,
            leaveMemberCount = leaveMembersCount
        )

        memberStatusRepository.save(memberStatus)

        discordLogger.log(
            title = "Daily Member Status Job Completed",
            message = "Member status recorded: New Members = $newMembersCount, Active Members = $activeMembersCount, Leave Members = $leaveMembersCount, Total Members = $totalMembersCount"
        )
    }
}