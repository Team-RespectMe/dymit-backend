package net.noti_me.dymit.dymit_backend_api.study

import io.kotest.core.spec.style.BehaviorSpec

class KotestBehaviorSpec : BehaviorSpec({

    beforeEach {
        println("Before each")
    }

    afterEach {
        println("After each")
    }

    Given("First given") {
        println("Executing first given block")
        When("First when") {
            println("Executing first when block")
            Then("First then") {
                println("Executing first then block")
            }
        }

        When("Second when") {
            println("Executing second when block")
            Then("Second then") {
                println("Executing second then block")
            }
        }
    }
})