package net.noti_me.dymit.dymit_backend_api.units.calendar

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.calendar.application.*
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group.LoadCalendarStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group.dto.CalendarStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule.LoadCalendarStudySchedulePort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule.dto.CalendarStudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.*
import net.noti_me.dymit.dymit_backend_api.calendar.domain.CalendarEventType
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task.LoadCalendarTaskPort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task.dto.CalendarTaskDto
import java.time.Instant
import java.time.LocalDate

internal class CalendarServiceTest : BehaviorSpec() {

    private val groups = mockk<LoadCalendarStudyGroupPort>()
    private val schedules = mockk<LoadCalendarStudySchedulePort>()
    private val tasks = mockk<LoadCalendarTaskPort>()
    private val query = CalendarEventQueryService(groups, schedules, tasks)
    private val memberId = "member"

    init {
        beforeEach { every { tasks.loadActiveTasks(any(), any(), any(), any()) } returns emptyList() }
        afterEach { clearAllMocks() }

        Given("서울 날짜 경계와 여러 그룹의 정렬되지 않은 일정") {
            Then("기간 밖과 소속 외 일정을 제외하고 그룹 이름 및 시각과 식별자 순서를 보존한다") {
                val date = LocalDate.parse("2026-10-02")
                val period = CalendarPeriod.daily(date)
                val start = Instant.parse("2026-10-01T15:00:00Z")
                period.startInclusive shouldBe start
                period.endExclusive shouldBe Instant.parse("2026-10-02T15:00:00Z")
                every { groups.loadActiveGroupsByMemberId(memberId) } returns listOf(
                    CalendarStudyGroupDto("b", "두 번째"), CalendarStudyGroupDto("a", "첫 번째"),
                    CalendarStudyGroupDto("c", "일정 없는 그룹")
                )
                every { schedules.loadActiveSchedules(any(), any(), any(), any()) } returns listOf(
                    CalendarStudyScheduleDto("z", "a", "늦은 일정", start.plusSeconds(3600)),
                    CalendarStudyScheduleDto("b", "a", "동시 일정 둘", start),
                    CalendarStudyScheduleDto("a", "a", "동시 일정 하나", start),
                    CalendarStudyScheduleDto("x", "b", "둘째 그룹 일정", start.plusSeconds(1)),
                    CalendarStudyScheduleDto("before", "a", "시작 전", start.minusNanos(1)),
                    CalendarStudyScheduleDto("end", "a", "종료 경계", period.endExclusive),
                    CalendarStudyScheduleDto("foreign", "other", "다른 소속", start)
                )

                val result = query.loadEvents(memberId, period)

                result.map { it.group.id } shouldBe listOf("a", "a", "a", "b")
                result.map { it.group.name } shouldBe listOf("첫 번째", "첫 번째", "첫 번째", "두 번째")
                result.map { it.id } shouldBe listOf("z", "a", "b", "x")
                result.map { it.eventAt } shouldBe listOf(start.plusSeconds(3600), start, start, start.plusSeconds(1))
                result.all { it.type == CalendarEventType.STUDY_SCHEDULE } shouldBe true
                query.loadEventDates(memberId, period) shouldBe setOf(date)
                verify(exactly = 2) {
                    schedules.loadActiveSchedules(memberId, listOf("b", "a", "c"), period.startInclusive, period.endExclusive)
                }
            }
        }

        Given("참가 일정 없이 본인 제출 대상 과제만 있는 날") {
            Then("과제만으로 날짜와 그룹을 만들고 본인 및 동일 기간을 두 포트에 전달한다") {
                val date = LocalDate.parse("2026-10-02")
                val period = CalendarPeriod.daily(date)
                every { groups.loadActiveGroupsByMemberId(memberId) } returns listOf(CalendarStudyGroupDto("a", "그룹 이름"))
                every { schedules.loadActiveSchedules(any(), any(), any(), any()) } returns emptyList()
                every { tasks.loadActiveTasks(any(), any(), any(), any()) } returns listOf(
                    CalendarTaskDto("task", "a", "제출 과제", period.startInclusive),
                    CalendarTaskDto("foreign", "other", "다른 그룹", period.startInclusive),
                    CalendarTaskDto("before", "a", "시작 전", period.startInclusive.minusNanos(1)),
                    CalendarTaskDto("end", "a", "종료 경계", period.endExclusive)
                )
                val result = query.loadEvents(memberId, period)
                result.single().group.name shouldBe "그룹 이름"
                result.single().type shouldBe CalendarEventType.TASK
                result.single().id shouldBe "task"
                result.single().eventAt shouldBe period.startInclusive
                query.loadEventDates(memberId, period) shouldBe setOf(date)
                verify(exactly = 2) { tasks.loadActiveTasks(memberId, listOf("a"), period.startInclusive, period.endExclusive) }
                verify(exactly = 2) { schedules.loadActiveSchedules(memberId, listOf("a"), period.startInclusive, period.endExclusive) }
            }
        }
        Given("여러 그룹의 뒤섞인 일정과 과제에 시각과 식별자 동률이 있으면") {
            Then("두 유형을 합친 뒤 시각 내림차순·식별자·유형 순으로 정렬한다") {
                val period = CalendarPeriod.daily(LocalDate.parse("2026-10-02"))
                val at = period.startInclusive
                every { groups.loadActiveGroupsByMemberId(memberId) } returns listOf(CalendarStudyGroupDto("b", "둘째 그룹"), CalendarStudyGroupDto("a", "첫 그룹"))
                every { schedules.loadActiveSchedules(any(), any(), any(), any()) } returns listOf(
                    CalendarStudyScheduleDto("b-schedule", "b", "둘째 그룹 일정", at.plusSeconds(10)),
                    CalendarStudyScheduleDto("same", "a", "일정", at),
                    CalendarStudyScheduleDto("early", "a", "가장 이른 일정", at.plusSeconds(1))
                )
                every { tasks.loadActiveTasks(any(), any(), any(), any()) } returns listOf(
                    CalendarTaskDto("b-task", "b", "둘째 그룹 과제", at.plusSeconds(20)),
                    CalendarTaskDto("same", "a", "과제", at),
                    CalendarTaskDto("a", "a", "식별자 우선", at),
                    CalendarTaskDto("later", "a", "늦은 과제", at.plusSeconds(2))
                )
                val events = query.loadEvents(memberId, period)
                events.map { it.group.id } shouldBe listOf("a", "a", "a", "a", "a", "b", "b")
                events.map { it.id to it.type } shouldBe listOf(
                    "later" to CalendarEventType.TASK, "early" to CalendarEventType.STUDY_SCHEDULE,
                    "a" to CalendarEventType.TASK, "same" to CalendarEventType.STUDY_SCHEDULE,
                    "same" to CalendarEventType.TASK, "b-task" to CalendarEventType.TASK,
                    "b-schedule" to CalendarEventType.STUDY_SCHEDULE
                )
            }
        }
        Given("현재 소속 그룹이 없는 회원") {
            Then("일정을 조회하지 않고 빈 결과를 반환한다") {
                every { groups.loadActiveGroupsByMemberId(memberId) } returns emptyList()
                query.loadEvents(memberId, CalendarPeriod.daily(LocalDate.parse("2026-10-02"))) shouldBe emptyList()
                verify(exactly = 0) { schedules.loadActiveSchedules(any(), any(), any(), any()) }
                verify(exactly = 0) { tasks.loadActiveTasks(any(), any(), any(), any()) }
            }
        }

        Given("소속 그룹에 일정이 없는 기간") {
            Then("빈 날짜별 결과를 반환한다") {
                every { groups.loadActiveGroupsByMemberId(memberId) } returns listOf(CalendarStudyGroupDto("a", "그룹"))
                every { schedules.loadActiveSchedules(any(), any(), any(), any()) } returns emptyList()
                query.loadEvents(memberId, CalendarPeriod.weekly(LocalDate.parse("2026-10-02"))) shouldBe emptyList()
            }
        }

        listOf("2026-09-27", "2026-10-03", "2027-01-01").forEach { input ->
            Given("일요일·토요일·연도 경계 기준일 $input") {
                Then("일요일부터 서울 자정 기준 7일의 빈 주를 반환한다") {
                    val mockedQuery = mockk<CalendarEventQueryService>()
                    val captured = slot<CalendarPeriod>()
                    every { mockedQuery.loadEventDates(memberId, capture(captured)) } returns emptySet()
                    val expectedStart = LocalDate.parse(if (input == "2027-01-01") "2026-12-27" else "2026-09-27")
                    val result = GetCalendarWeeklyPresenceService(mockedQuery).execute(
                        GetCalendarWeeklyPresenceCommand(memberId, LocalDate.parse(input))
                    )
                    result.map { it.date } shouldBe (0L..6L).map { expectedStart.plusDays(it) }
                    result.all { !it.hasEvents } shouldBe true
                    captured.captured.startInclusive shouldBe expectedStart.atStartOfDay(CalendarQueryDateParser.KOREA_ZONE).toInstant()
                    captured.captured.endExclusive shouldBe expectedStart.plusDays(7).atStartOfDay(CalendarQueryDateParser.KOREA_ZONE).toInstant()
                }
            }
        }

        Given("본인 과제만 있는 날짜의 주간과 일별 조회") {
            Then("주간은 상세 조회 없이 존재 여부만 반환하고 일별은 과제를 반환한다") {
                val mockedQuery = mockk<CalendarEventQueryService>()
                val date = LocalDate.parse("2026-10-02")
                val task = CalendarEventDto(CalendarEventType.TASK, "task", "본인 과제", Instant.parse("2026-10-02T00:00:00Z"), CalendarEventGroupDto("a", "그룹"))
                every { mockedQuery.loadEventDates(memberId, CalendarPeriod.weekly(date)) } returns setOf(date)
                every { mockedQuery.loadEvents(memberId, CalendarPeriod.daily(date)) } returns listOf(task)
                val weekly = GetCalendarWeeklyPresenceService(mockedQuery).execute(GetCalendarWeeklyPresenceCommand(memberId, date))
                weekly.single { it.date == date }.hasEvents shouldBe true
                weekly.count { it.hasEvents } shouldBe 1
                verify(exactly = 0) { mockedQuery.loadEvents(any(), any()) }
                val daily = GetCalendarDailyEventsService(mockedQuery).execute(GetCalendarDailyEventsCommand(memberId, date))
                daily shouldBe listOf(task)
            }
        }
        Given("일별 조회 날짜에 이벤트가 없으면") {
            Then("정확한 하루 범위를 조회하고 빈 목록을 반환한다") {
                val mockedQuery = mockk<CalendarEventQueryService>()
                val date = LocalDate.parse("2026-10-02")
                every { mockedQuery.loadEvents(memberId, CalendarPeriod.daily(date)) } returns emptyList()
                GetCalendarDailyEventsService(mockedQuery).execute(GetCalendarDailyEventsCommand(memberId, date)) shouldBe emptyList()
                verify(exactly = 1) { mockedQuery.loadEvents(memberId, CalendarPeriod.daily(date)) }
            }
        }
    }
}
