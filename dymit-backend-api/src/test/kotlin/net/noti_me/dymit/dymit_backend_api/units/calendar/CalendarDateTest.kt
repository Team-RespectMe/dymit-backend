package net.noti_me.dymit.dymit_backend_api.units.calendar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.calendar.application.CalendarPeriod
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.GetCalendarDailyEventsCommand
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.GetCalendarWeeklyPresenceCommand
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import java.time.LocalDate

internal class CalendarDateTest : BehaviorSpec() {

    init {
        Given("기본 날짜를 명시한 날짜 생략 명령") {
            Then("두 명령은 전달받은 기본 날짜를 사용한다") {
                val today = LocalDate.parse("2026-10-02")
                GetCalendarWeeklyPresenceCommand.from("member", null, today).date shouldBe today
                GetCalendarDailyEventsCommand.from("member", null, today).date shouldBe today
            }
        }
        Given("MongoDB 날짜의 밀리초 범위를 넘어서는 유효한 ISO 날짜") {
            Then("일별과 주간 조회는 DB 변환 이전에 400 오류로 실패한다") {
                val date = LocalDate.parse("+999999998-10-02")
                shouldThrow<BadRequestException> { CalendarPeriod.daily(date) }.status shouldBe 400
                shouldThrow<BadRequestException> { CalendarPeriod.weekly(date) }.status shouldBe 400
            }
        }
        Given("계산 가능한 범위를 넘어서는 극단 날짜") {
            Then("주간과 일별 종료일 계산은 400 오류로 실패한다") {
                shouldThrow<BadRequestException> { CalendarPeriod.weekly(LocalDate.MAX) }.status shouldBe 400
                shouldThrow<BadRequestException> { CalendarPeriod.daily(LocalDate.MAX) }.status shouldBe 400
                shouldThrow<BadRequestException> { CalendarPeriod.weekly(LocalDate.MIN) }.status shouldBe 400
            }
        }
    }
}
