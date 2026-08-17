package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.QueryStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.QueryStudyRecruitmentRequest

/** Query study recruitment web-request conversion unit tests. */
internal class QueryStudyRecruitmentUseCaseImplTest : BehaviorSpec() {

    init {
        Given("a web query request") {
            When("it is converted at the web boundary") {
                val command = QueryStudyRecruitmentRequest(cursor = "cursor-id", size = 7).toCommand()

                Then("it preserves the cursor and requested response size in the input command") {
                    command shouldBe QueryStudyRecruitmentCommand(cursor = "cursor-id", size = 7)
                }
            }
        }

        Given("a web query request using defaults") {
            When("it is converted at the web boundary") {
                val command = QueryStudyRecruitmentRequest().toCommand()

                Then("it preserves the REST defaults") {
                    command shouldBe QueryStudyRecruitmentCommand(cursor = null, size = 20)
                }
            }
        }
    }
}
