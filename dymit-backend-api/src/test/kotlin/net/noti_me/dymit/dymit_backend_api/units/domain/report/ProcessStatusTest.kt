package net.noti_me.dymit.dymit_backend_api.units.domain.report

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus

class ProcessStatusTest : BehaviorSpec({

    given("ProcessStatus enum이 주어졌을 때") {
        `when`("모든 enum 값들을 확인할 때") {
            then("REPORTED 값이 존재한다") {
                ProcessStatus.REPORTED.name shouldBe "REPORTED"
            }

            then("PROCESSED 값이 존재한다") {
                ProcessStatus.PROCESSED.name shouldBe "PROCESSED"
            }

            then("REJECTED 값이 존재한다") {
                ProcessStatus.REJECTED.name shouldBe "REJECTED"
            }

            then("총 3개의 enum 값이 있다") {
                ProcessStatus.values().size shouldBe 3
            }

            then("values()로 모든 값을 가져올 수 있다") {
                val values = ProcessStatus.values()
                values.contains(ProcessStatus.REPORTED) shouldBe true
                values.contains(ProcessStatus.PROCESSED) shouldBe true
                values.contains(ProcessStatus.REJECTED) shouldBe true
            }

            then("valueOf()로 특정 값을 가져올 수 있다") {
                ProcessStatus.valueOf("REPORTED") shouldBe ProcessStatus.REPORTED
                ProcessStatus.valueOf("PROCESSED") shouldBe ProcessStatus.PROCESSED
                ProcessStatus.valueOf("REJECTED") shouldBe ProcessStatus.REJECTED
            }
        }
    }
})
