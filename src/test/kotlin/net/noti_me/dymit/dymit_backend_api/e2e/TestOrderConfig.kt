package net.noti_me.dymit.dymit_backend_api.e2e

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.SpecExecutionOrder

class TestOrderConfig : AbstractProjectConfig() {

    override val specExecutionOrder: SpecExecutionOrder = SpecExecutionOrder.Annotated
}
