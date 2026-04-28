package net.noti_me.dymit.dymit_backend_api.units.domain.report

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.domain.report.ResourceType

class ResourceTypeTest : BehaviorSpec({

    given("ResourceType enum이 주어졌을 때") {
        `when`("모든 enum 값들을 확인할 때") {
            then("STUDY_GROUP 값이 존재한다") {
                ResourceType.STUDY_GROUP.name shouldBe "STUDY_GROUP"
            }

            then("총 4개의 enum 값이 있다") {
                ResourceType.values().size shouldBe 4
            }

            then("values()로 모든 값을 가져올 수 있다") {
                val values = ResourceType.values()
                values.contains(ResourceType.STUDY_GROUP) shouldBe true
            }

            then("valueOf()로 특정 값을 가져올 수 있다") {
                ResourceType.valueOf("STUDY_GROUP") shouldBe ResourceType.STUDY_GROUP
            }
        }
    }
})
