package net.noti_me.dymit.dymit_backend_api.units.study_schedule.adapter.`in`.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto.StudyScheduleAttachmentReplaceRequest
import org.bson.types.ObjectId

internal class StudyScheduleAttachmentReplaceRequestTest : BehaviorSpec({

    Given("첨부 일괄 교체 요청 DTO가 주어지면") {
        val scheduleId = ObjectId.get().toHexString()
        val fileIds = listOf(ObjectId.get().toHexString(), ObjectId.get().toHexString())
        val request = StudyScheduleAttachmentReplaceRequest(fileIds = fileIds)

        When("toCommand를 호출하면") {
            val command = request.toCommand(scheduleId)

            Then("scheduleId와 fileIds가 그대로 전달된다") {
                command.scheduleId shouldBe scheduleId
                command.fileIds shouldBe fileIds
            }
        }
    }
})
