package net.noti_me.dymit.dymit_backend_api.units.application.userFeed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.user_feed.UserFeedServiceImpl
import net.noti_me.dymit.dymit_backend_api.application.user_feed.commands.DeleteUserFeedCommand
import net.noti_me.dymit.dymit_backend_api.application.user_feed.queries.GetUserFeedListQuery
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.ports.outbound.UserFeedRepository
import org.bson.types.ObjectId

class UserFeedServiceTest : BehaviorSpec({

    val userFeedRepository = mockk<UserFeedRepository>()
    val userFeedService = UserFeedServiceImpl(userFeedRepository)

    given("피드 삭제") {
        `when`("본인의 피드를 삭제하려고 할 때") {
            val memberId = ObjectId()
            val feedId = ObjectId()
            val userFeed = UserFeed(
                id = feedId,
                memberId = memberId,
                message = "테스트 피드",
                associated = AssociatedResource(type = "POST", resourceId = "post123")
            )
            val command = DeleteUserFeedCommand(feedId = feedId, requesterId = memberId)

            every { userFeedRepository.findById(feedId) } returns userFeed
            every { userFeedRepository.deleteById(feedId) } returns Unit

            userFeedService.deleteFeed(command)

            then("정상적으로 삭제되어야 한다") {
                verify { userFeedRepository.findById(feedId) }
                verify { userFeedRepository.deleteById(feedId) }
            }
        }

        `when`("다른 사용자의 피드를 삭제하려고 할 때") {
            val ownerId = ObjectId()
            val requesterId = ObjectId()
            val feedId = ObjectId()
            val userFeed = UserFeed(
                id = feedId,
                memberId = ownerId,
                message = "다른 사용자의 피드",
                associated = AssociatedResource(type = "POST", resourceId = "post123")
            )
            val command = DeleteUserFeedCommand(feedId = feedId, requesterId = requesterId)

            every { userFeedRepository.findById(feedId) } returns userFeed

            then("권한 없음 예외가 발생해야 한다") {
                shouldThrow<IllegalAccessException> {
                    userFeedService.deleteFeed(command)
                }
            }
        }

        `when`("존재하지 않는 피드를 삭제하려고 할 때") {
            val feedId = ObjectId()
            val requesterId = ObjectId()
            val command = DeleteUserFeedCommand(feedId = feedId, requesterId = requesterId)

            every { userFeedRepository.findById(feedId) } returns null

            then("피드를 찾을 수 없음 예외가 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    userFeedService.deleteFeed(command)
                }
            }
        }
    }

    given("피드 목록 조회") {
        `when`("본인의 피드 목록을 조회할 때") {
            val memberId = ObjectId()
            val userFeeds = listOf(
                UserFeed(
                    memberId = memberId,
                    message = "첫 번째 피드",
                    associated = AssociatedResource(type = "POST", resourceId = "post1")
                ),
                UserFeed(
                    memberId = memberId,
                    message = "두 번째 피드",
                    associated = AssociatedResource(type = "COMMENT", resourceId = "comment1")
                )
            )
            val query = GetUserFeedListQuery(memberId = memberId, requesterId = memberId)

            every { userFeedRepository.findByMemberId(memberId) } returns userFeeds

            val result = userFeedService.getFeedList(query)

            then("정상적으로 피드 목록이 반환되어야 한다") {
                result shouldHaveSize 2
                result[0].message shouldBe "첫 번째 피드"
                result[1].message shouldBe "두 번째 피드"
                verify { userFeedRepository.findByMemberId(memberId) }
            }
        }

        `when`("다른 사용자의 피드 목록을 조회하려고 할 때") {
            val memberId = ObjectId()
            val requesterId = ObjectId()
            val query = GetUserFeedListQuery(memberId = memberId, requesterId = requesterId)

            then("권한 없음 예외가 발생해야 한다") {
                shouldThrow<IllegalAccessException> {
                    userFeedService.getFeedList(query)
                }
            }
        }
    }
})
