package net.noti_me.dymit.dymit_backend_api.units.application.user_feed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.user_feed.impl.UserFeedServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.bson.types.ObjectId

/**
 * UserFeedServiceImpl 클래스의 비즈니스 로직을 테스트한다.
 */
class UserFeedServiceImplTest : BehaviorSpec({

    val userFeedRepository = mockk<UserFeedRepository>()
    val userFeedService = UserFeedServiceImpl(userFeedRepository)

    val testMemberId = ObjectId().toHexString()
    val testFeedId = ObjectId().toHexString()
    val otherMemberId = ObjectId().toHexString()

    lateinit var memberInfo: MemberInfo
    lateinit var testUserFeed: UserFeed
    lateinit var testAssociatedResources: List<AssociatedResource>

    beforeEach {
        // 테스트용 MemberInfo 생성
        memberInfo = MemberInfo(
            memberId = testMemberId,
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )

        // 테스트용 AssociatedResource 생성
        testAssociatedResources = listOf(
            AssociatedResource(
                type = ResourceType.STUDY_GROUP,
                resourceId = ObjectId().toHexString()
            )
        )

        // 테스트용 UserFeed 생성
        testUserFeed = UserFeed(
            id = ObjectId(testFeedId),
            memberId = ObjectId(testMemberId),
            message = "테스트 피드 메시지입니다.",
            associates = testAssociatedResources,
            isRead = false
        )
    }

    given("getUserFeeds 메서드가 호출될 때") {
        `when`("올바른 파라미터로 호출하면") {
            then("해당 회원의 피드 목록을 반환한다") {
                // given
                val cursor = null
                val size = 10
                val expectedFeeds = listOf(testUserFeed)
                every {
                    userFeedRepository.findByMemberId(testMemberId, cursor, size.toLong())
                } returns expectedFeeds

                // when
                val result = userFeedService.getUserFeeds(memberInfo, cursor, size)

                // then
                result.size shouldBe 1
                result[0].id shouldBe testFeedId
                result[0].memberId shouldBe testMemberId
                result[0].message shouldBe "테스트 피드 메시지입니다."
                result[0].isRead shouldBe false
                verify { userFeedRepository.findByMemberId(testMemberId, cursor, size.toLong()) }
            }
        }

        `when`("커서와 함께 호출하면") {
            then("커서 이후의 피드 목록을 반환한다") {
                // given
                val cursor = ObjectId().toHexString()
                val size = 5
                val expectedFeeds = listOf(testUserFeed)
                every {
                    userFeedRepository.findByMemberId(testMemberId, cursor, size.toLong())
                } returns expectedFeeds

                // when
                val result = userFeedService.getUserFeeds(memberInfo, cursor, size)

                // then
                result.size shouldBe 1
                verify { userFeedRepository.findByMemberId(testMemberId, cursor, size.toLong()) }
            }
        }

        `when`("피드가 없는 경우") {
            then("빈 목록을 반환한다") {
                // given
                val cursor = null
                val size = 10
                every {
                    userFeedRepository.findByMemberId(testMemberId, cursor, size.toLong())
                } returns emptyList()

                // when
                val result = userFeedService.getUserFeeds(memberInfo, cursor, size)

                // then
                result shouldBe emptyList()
                verify { userFeedRepository.findByMemberId(testMemberId, cursor, size.toLong()) }
            }
        }
    }

    given("deleteUserFeed 메서드가 호출될 때") {
        `when`("본인의 피드를 삭제하려고 하면") {
            then("정상적으로 삭제된다") {
                // given
                every { userFeedRepository.findById(testFeedId) } returns testUserFeed
                every { userFeedRepository.deleteById(testFeedId) } returns true

                // when
                userFeedService.deleteUserFeed(memberInfo, testFeedId)

                // then
                verify { userFeedRepository.findById(testFeedId) }
                verify { userFeedRepository.deleteById(testFeedId) }
            }
        }

        `when`("존재하지 않는 피드를 삭제하려고 하면") {
            then("NotFoundException이 발생한다") {
                // given
                every { userFeedRepository.findById(testFeedId) } returns null

                // when & then
                val exception = shouldThrow<NotFoundException> {
                    userFeedService.deleteUserFeed(memberInfo, testFeedId)
                }
                exception.message shouldBe "요청한 리소스를 찾을 수 없습니다."
                verify { userFeedRepository.findById(testFeedId) }
            }
        }

        `when`("다른 사용자의 피드를 삭제하려고 하면") {
            then("ForbiddenException이 발생한다") {
                // given
                val otherUserFeed = UserFeed(
                    id = ObjectId(testFeedId),
                    memberId = ObjectId(otherMemberId),
                    message = "다른 사용자의 피드",
                    associates = testAssociatedResources,
                    isRead = false
                )
                every { userFeedRepository.findById(testFeedId) } returns otherUserFeed

                // when & then
                val exception = shouldThrow<ForbiddenException> {
                    userFeedService.deleteUserFeed(memberInfo, testFeedId)
                }
                exception.message shouldBe "피드 삭제 권한이 없습니다."
                verify { userFeedRepository.findById(testFeedId) }
            }
        }
    }

    given("markFeedAsRead 메서드가 호출될 때") {
        `when`("본인의 읽지 않은 피드를 읽음 처리하려고 하면") {
            then("정상적으로 읽음 처리된다") {
                // given
                every { userFeedRepository.findById(testFeedId) } returns testUserFeed
                every { userFeedRepository.save(any()) } returns testUserFeed

                // when
                userFeedService.markFeedAsRead(memberInfo, testFeedId)

                // then
                testUserFeed.isRead shouldBe true
                verify { userFeedRepository.findById(testFeedId) }
                verify { userFeedRepository.save(testUserFeed) }
            }
        }

        `when`("이미 읽은 피드를 읽음 처리하려고 하면") {
            then("중복 처리되지 않고 정상적으로 완료된다") {
                // given
                testUserFeed.markAsRead() // 미리 읽음 처리
                every { userFeedRepository.findById(testFeedId) } returns testUserFeed
                every { userFeedRepository.save(any()) } returns testUserFeed

                // when
                userFeedService.markFeedAsRead(memberInfo, testFeedId)

                // then
                testUserFeed.isRead shouldBe true
                verify { userFeedRepository.findById(testFeedId) }
                verify { userFeedRepository.save(testUserFeed) }
            }
        }

        `when`("존재하지 않는 피드를 읽음 처리하려고 하면") {
            then("NotFoundException이 발생한다") {
                // given
                every { userFeedRepository.findById(testFeedId) } returns null

                // when & then
                val exception = shouldThrow<NotFoundException> {
                    userFeedService.markFeedAsRead(memberInfo, testFeedId)
                }
                exception.message shouldBe "피드를 찾을 수 없습니다."
                verify { userFeedRepository.findById(testFeedId) }
            }
        }

        `when`("다른 사용자의 피드를 읽음 처리하려고 하면") {
            then("ForbiddenException이 발생한다") {
                // given
                val otherUserFeed = UserFeed(
                    id = ObjectId(testFeedId),
                    memberId = ObjectId(otherMemberId),
                    message = "다른 사용자의 피드",
                    associates = testAssociatedResources,
                    isRead = false
                )
                every { userFeedRepository.findById(testFeedId) } returns otherUserFeed

                // when & then
                val exception = shouldThrow<ForbiddenException> {
                    userFeedService.markFeedAsRead(memberInfo, testFeedId)
                }
                exception.message shouldBe "피드 읽음 처리 권한이 없습니다."
                verify { userFeedRepository.findById(testFeedId) }
            }
        }
    }
})
