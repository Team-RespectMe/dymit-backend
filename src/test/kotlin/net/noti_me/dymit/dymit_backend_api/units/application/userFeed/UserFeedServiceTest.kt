package net.noti_me.dymit.dymit_backend_api.units.application.userFeed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.user_feed.impl.UserFeedServiceImpl
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.bson.types.ObjectId

class UserFeedServiceTest : BehaviorSpec({

    val userFeedRepository = mockk<UserFeedRepository>()
    val userFeedService = UserFeedServiceImpl(userFeedRepository)

}
)