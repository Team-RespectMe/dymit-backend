package net.noti_me.dymit.dymit_backend_api.supports

import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.study_group.application.RefreshMemberStatisticsService
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.*
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.RefreshMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.domain.*
import org.bson.types.ObjectId
import java.time.Instant

internal class StatisticsTestFixture {

    val groupId = ObjectId()
    val membershipId = ObjectId()
    val memberId = ObjectId()
    val joinedAt: Instant = Instant.parse("2026-09-07T00:00:00Z")
    val cutoff: Instant = Instant.parse("2026-09-27T15:00:00Z")
    val observedAt: Instant = Instant.parse("2026-10-03T01:00:00Z")
    val repository = mockk<StatisticsRepository>()
    val members = mockk<StudyGroupMemberRepository>()
    val tasks = mockk<TaskStatisticsSourcePort>()
    val schedules = mockk<ScheduleStatisticsSourcePort>()
    var ledger: MemberStatisticsLedger? = null
    val weeks = mutableListOf<MemberWeeklyStatistics>()
    var allTasks = emptyList<TaskStatisticsSourceData>()
    var allSchedules = emptyList<ScheduleStatisticsSourceData>()
    var taskChanges = StatisticsSourceChanges<TaskStatisticsSourceData>(emptyList(), false)
    var scheduleChanges = StatisticsSourceChanges<ScheduleStatisticsSourceData>(emptyList(), false)
    val service = RefreshMemberStatisticsService(repository, members, tasks, schedules)

    init {
        every { members.findByIdIncludingDeleted(membershipId) } returns StudyGroupMember(
            id = membershipId, groupId = groupId, memberId = memberId, createdAt = joinedAt
        )
        every { repository.findLedger(membershipId) } answers { ledger }
        every { repository.findMemberWeeks(membershipId, any()) } answers {
            weeks.filter { it.weekEnd <= secondArg<Instant>() }
        }
        every { repository.compareAndSetLedger(any(), any()) } answers {
            val expected = firstArg<Long?>()
            if (expected != ledger?.version) {
                false
            } else {
                ledger = secondArg()
                true
            }
        }
        every { repository.saveMemberWeeks(any()) } answers {
            firstArg<List<MemberWeeklyStatistics>>().forEach { next ->
                val previous = weeks.firstOrNull { it.weekEnd == next.weekEnd }
                if (previous == null || next.ledgerVersion >= previous.ledgerVersion) {
                    weeks.removeAll { it.weekEnd == next.weekEnd }
                    weeks.add(next)
                }
            }
        }
        every { tasks.loadAll(any()) } answers { allTasks }
        every { schedules.loadAll(any()) } answers { allSchedules }
        every { tasks.loadChanged(any()) } answers { taskChanges }
        every { schedules.loadChanged(any()) } answers { scheduleChanges }
    }

    fun command(
        end: Instant = cutoff,
        observed: Instant = observedAt
    ) = RefreshMemberStatisticsCommand(groupId.toHexString(), membershipId.toHexString(), end, observed)

    fun task(
        expires: Instant,
        submittedAt: Instant? = null,
        deletedAt: Instant? = null,
        assignedAt: Instant = joinedAt,
        history: List<TaskStatusHistoryData> = submittedAt?.let { listOf(TaskStatusHistoryData(true, it)) }
            ?: emptyList()
    ) = TaskStatisticsSourceData(
        ObjectId(), expires, emptyList(), deletedAt, null,
        listOf(TaskAssignmentData(assignedAt, null, history, false, null))
    )

    fun schedule(
        starts: Instant,
        created: Instant = joinedAt,
        attendedAt: Instant? = starts.minusSeconds(1),
        deletedAt: Instant? = null
    ) = ScheduleStatisticsSourceData(
        ObjectId(), created, starts, deletedAt,
        attendedAt?.let { listOf(ScheduleParticipationData(it, null)) } ?: emptyList()
    )
}
