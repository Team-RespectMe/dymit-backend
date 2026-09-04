package net.noti_me.dymit.dymit_backend_api.units.study_schedule.adapter.`in`.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleDetailDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto.StudyScheduleResponse
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleLocation
import java.time.Instant

internal class StudyScheduleResponseTest : BehaviorSpec({

    Given("스터디 일정 상세 DTO가 주어지면") {
        val scheduleId = "688c25eb2f3a71dcf291aac9"
        val dto = StudyScheduleDetailDto(
            id = scheduleId,
            session = 3L,
            title = "자료 공유 세션",
            description = "첨부 링크 확인",
            scheduleAt = Instant.parse("2026-05-03T19:30:00Z"),
            location = LocationVo(
                type = ScheduleLocation.LocationType.ONLINE,
                value = "Zoom",
                link = "https://zoom.us/j/123"
            ),
            participants = emptyList(),
            roles = emptyList(),
            attending = true
        )

        When("StudyScheduleResponse.from을 호출하면") {
            val response = StudyScheduleResponse.from(dto)

            Then("attachments HATEOAS 링크가 포함된다") {
                response.id shouldBe scheduleId
                response._links["attachments"]?.href shouldBe "/api/v1/study-schedules/$scheduleId/attachments"
            }
        }
    }
})
