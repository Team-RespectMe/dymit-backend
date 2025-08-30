package net.noti_me.dymit.dymit_backend_api.units.domain.report

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource
import net.noti_me.dymit.dymit_backend_api.domain.report.ResourceType
import org.bson.types.ObjectId

class ReportedResourceTest : BehaviorSpec({

    given("ReportedResource 인스턴스가 주어졌을 때") {
        val resourceId = ObjectId().toHexString()
        val reportedResource = ReportedResource(
            resourceType = ResourceType.STUDY_GROUP,
            resourceId = resourceId
        )

        `when`("ReportedResource가 생성될 때") {
            then("resourceType이 올바르게 설정된다") {
                reportedResource.resourceType shouldBe ResourceType.STUDY_GROUP
            }

            then("resourceId가 올바르게 설정된다") {
                reportedResource.resourceId shouldBe resourceId
            }
        }

        `when`("데이터 클래스의 특성을 테스트할 때") {
            then("같은 값으로 생성된 인스턴스는 동일하다") {
                val sameResource = ReportedResource(
                    resourceType = ResourceType.STUDY_GROUP,
                    resourceId = resourceId
                )
                reportedResource shouldBe sameResource
            }

            then("다른 resourceType을 가진 인스턴스는 다르다") {
                val differentTypeResource = ReportedResource(
                    resourceType = ResourceType.STUDY_GROUP,
                    resourceId = ObjectId().toHexString()
                )
                reportedResource shouldNotBe differentTypeResource
            }

            then("copy() 메서드가 정상적으로 동작한다") {
                val copiedResource = reportedResource.copy()
                copiedResource shouldBe reportedResource

                val modifiedResource = reportedResource.copy(resourceId = "new-id")
                modifiedResource.resourceType shouldBe ResourceType.STUDY_GROUP
                modifiedResource.resourceId shouldBe "new-id"
            }

            then("toString() 메서드가 정상적으로 동작한다") {
                val stringRepresentation = reportedResource.toString()
                stringRepresentation.contains("STUDY_GROUP") shouldBe true
                stringRepresentation.contains(resourceId) shouldBe true
            }
        }
    }

    given("다양한 ReportedResource 인스턴스들이 주어졌을 때") {
        `when`("여러 다른 리소스들을 생성할 때") {
            then("각각 다른 resourceId를 가질 수 있다") {
                val resource1 = ReportedResource(
                    resourceType = ResourceType.STUDY_GROUP,
                    resourceId = ObjectId().toHexString()
                )
                val resource2 = ReportedResource(
                    resourceType = ResourceType.STUDY_GROUP,
                    resourceId = ObjectId().toHexString()
                )

                resource1.resourceType shouldBe resource2.resourceType
                resource1.resourceId shouldNotBe resource2.resourceId
                resource1 shouldNotBe resource2
            }
        }
    }
})
