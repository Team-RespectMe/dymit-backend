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

internal class StatisticsTestFixture(
    val groupId: ObjectId = ObjectId(),
    val membershipId: ObjectId = ObjectId(),
    val memberId: ObjectId = ObjectId()
) {
    val joinedAt: Instant = Instant.parse("2026-09-07T00:00:00Z")
    val cutoff: Instant = Instant.parse("2026-09-27T15:00:00Z")
    val observedAt: Instant = Instant.parse("2026-10-03T01:00:00Z")
    val repository = mockk<StatisticsRepository>()
    val members = mockk<StudyGroupMemberRepository>()
    val tasks = mockk<TaskStatisticsSourcePort>()
    val schedules = mockk<ScheduleStatisticsSourcePort>()
    var ledger: MemberSessionStatisticsLedger? = null
    val sessions = mutableListOf<MemberSessionStatistics>()
    var boundaries = listOf(
        boundary(joinedAt.plusSeconds(86400), 3),
        boundary(cutoff.minusSeconds(604800), 8),
        boundary(cutoff, 12)
    )
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
        every { repository.findMemberSessions(membershipId) } answers {
            sessions.toList()
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
        every { repository.saveMemberSessions(any()) } answers {
            firstArg<List<MemberSessionStatistics>>().forEach { next ->
                val previous = sessions.firstOrNull { it.scheduleId == next.scheduleId }
                if (previous == null || next.ledgerVersion >= previous.ledgerVersion) {
                    sessions.removeAll { it.scheduleId == next.scheduleId }
                    sessions.add(next)
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
    ): RefreshMemberStatisticsCommand {
        if (boundaries.none { it.scheduleAt == end }) {
            boundaries = boundaries + boundary(end, (boundaries.maxOfOrNull { it.session } ?: 0) + 1)
        }
        return RefreshMemberStatisticsCommand(
            groupId.toHexString(), membershipId.toHexString(),
            boundaries.filter { it.scheduleAt <= end }, observed
        )
    }

    fun boundary(
        starts: Instant,
        session: Long = 1,
        id: ObjectId = ObjectId()
    ) = StatisticsSessionBoundary(groupId, id, session, starts)

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
        deletedAt: Instant? = null,
        id: ObjectId = ObjectId()
    ) = ScheduleStatisticsSourceData(
        id, 1, created, starts, deletedAt,
        attendedAt?.let { listOf(ScheduleParticipationData(it, null)) } ?: emptyList()
    )
}
