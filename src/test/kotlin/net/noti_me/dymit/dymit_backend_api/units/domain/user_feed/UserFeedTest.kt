package net.noti_me.dymit.dymit_backend_api.units.domain.user_feed

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.bson.types.ObjectId

/**
 * UserFeed 도메인 객체의 비즈니스 로직을 테스트하는 클래스
 */
class UserFeedTest : BehaviorSpec({

    given("markAsRead() 메서드 테스트") {

        `when`("읽지 않은 상태의 UserFeed에서 markAsRead()를 호출할 때") {
            val userFeed = UserFeed(
                id = ObjectId(),
                memberId = ObjectId(),
                message = "테스트 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                ),
                isRead = false
            )

            then("isRead가 false에서 true로 변경되어야 한다") {
                userFeed.isRead shouldBe false
                userFeed.markAsRead()
                userFeed.isRead shouldBe true
            }
        }

        `when`("이미 읽은 상태의 UserFeed에서 markAsRead()를 호출할 때") {
            val userFeed = UserFeed(
                id = ObjectId(),
                memberId = ObjectId(),
                message = "테스트 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                ),
                isRead = true
            )

            then("isRead가 true 상태를 유지해야 한다") {
                userFeed.isRead shouldBe true
                userFeed.markAsRead()
                userFeed.isRead shouldBe true
            }
        }

        `when`("markAsRead()를 여러 번 연속으로 호출할 때") {
            val userFeed = UserFeed(
                id = ObjectId(),
                memberId = ObjectId(),
                message = "테스트 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                ),
                isRead = false
            )

            then("여러 번 호출해도 isRead는 true 상태를 유지해야 한다") {
                userFeed.isRead shouldBe false
                userFeed.markAsRead()
                userFeed.isRead shouldBe true
                userFeed.markAsRead()
                userFeed.isRead shouldBe true
                userFeed.markAsRead()
                userFeed.isRead shouldBe true
            }
        }
    }

    given("isOwnedBy() 메서드 테스트") {
        val memberId = ObjectId()

        `when`("동일한 memberId로 isOwnedBy()를 호출할 때") {
            val userFeed = UserFeed(
                id = ObjectId(),
                memberId = memberId,
                message = "테스트 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                ),
                isRead = false
            )

            then("true를 반환해야 한다") {
                val result = userFeed.isOwnedBy(memberId.toHexString())
                result shouldBe true
            }
        }

        `when`("다른 memberId로 isOwnedBy()를 호출할 때") {
            val userFeed = UserFeed(
                id = ObjectId(),
                memberId = memberId,
                message = "테스트 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                ),
                isRead = false
            )
            val otherMemberId = ObjectId()

            then("false를 반환해야 한다") {
                val result = userFeed.isOwnedBy(otherMemberId.toHexString())
                result shouldBe false
            }
        }

        `when`("빈 문자열로 isOwnedBy()를 호출할 때") {
            val userFeed = UserFeed(
                id = ObjectId(),
                memberId = memberId,
                message = "테스트 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                ),
                isRead = false
            )

            then("false를 반환해야 한다") {
                val result = userFeed.isOwnedBy("")
                result shouldBe false
            }
        }

        `when`("null이나 유효하지 않은 ObjectId 형식으로 isOwnedBy()를 호출할 때") {
            val userFeed = UserFeed(
                id = ObjectId(),
                memberId = memberId,
                message = "테스트 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                ),
                isRead = false
            )

            then("유효하지 않은 형식의 문자열에 대해 false를 반환해야 한다") {
                val result = userFeed.isOwnedBy("invalid-object-id")
                result shouldBe false
            }
        }
    }

    given("UserFeed 생성자 및 속성 테스트") {

        `when`("모든 필수 속성으로 UserFeed를 생성할 때") {
            val id = ObjectId()
            val memberId = ObjectId()
            val message = "테스트 피드 메시지"
            val associates = listOf(
                AssociatedResource(ResourceType.STUDY_GROUP, "group1"),
                AssociatedResource(ResourceType.MEMBER, "member1")
            )

            val userFeed = UserFeed(
                id = id,
                memberId = memberId,
                message = message,
                associates = associates,
                isRead = false
            )

            then("모든 속성이 올바르게 설정되어야 한다") {
                userFeed.id shouldBe id
                userFeed.memberId shouldBe memberId
                userFeed.message shouldBe message
                userFeed.associates shouldBe associates
                userFeed.isRead shouldBe false
            }
        }

        `when`("isRead를 true로 설정하여 UserFeed를 생성할 때") {
            val userFeed = UserFeed(
                id = ObjectId(),
                memberId = ObjectId(),
                message = "읽은 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                ),
                isRead = true
            )

            then("isRead가 true로 설정되어야 한다") {
                userFeed.isRead shouldBe true
            }
        }

        `when`("isRead를 명시하지 않고 UserFeed를 생성할 때") {
            val userFeed = UserFeed(
                memberId = ObjectId(),
                message = "기본 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.MEMBER, "member1")
                )
            )

            then("isRead가 기본값 false로 설정되어야 한다") {
                userFeed.isRead shouldBe false
            }
        }

        `when`("빈 associates 리스트로 UserFeed를 생성할 때") {
            val userFeed = UserFeed(
                memberId = ObjectId(),
                message = "연관 리소스가 없는 피드",
                associates = emptyList()
            )

            then("associates가 빈 리스트여야 한다") {
                userFeed.associates shouldBe emptyList()
                userFeed.associates.size shouldBe 0
            }
        }

        `when`("다양한 ResourceType의 associates로 UserFeed를 생성할 때") {
            val associates = listOf(
                AssociatedResource(ResourceType.MEMBER, "member123"),
                AssociatedResource(ResourceType.STUDY_GROUP, "group456"),
                AssociatedResource(ResourceType.STUDY_GROUP_MEMBER, "groupMember789"),
                AssociatedResource(ResourceType.STUDY_GROUP_SCHEDULE, "schedule101"),
                AssociatedResource(ResourceType.STUDY_GROUP_POST, "post202"),
                AssociatedResource(ResourceType.STUDY_GROUP_POST_COMMENT, "comment303")
            )

            val userFeed = UserFeed(
                memberId = ObjectId(),
                message = "다양한 리소스 타입 테스트",
                associates = associates
            )

            then("모든 ResourceType이 올바르게 설정되어야 한다") {
                userFeed.associates.size shouldBe 6
                userFeed.associates[0].type shouldBe ResourceType.MEMBER
                userFeed.associates[0].resourceId shouldBe "member123"
                userFeed.associates[1].type shouldBe ResourceType.STUDY_GROUP
                userFeed.associates[1].resourceId shouldBe "group456"
                userFeed.associates[2].type shouldBe ResourceType.STUDY_GROUP_MEMBER
                userFeed.associates[2].resourceId shouldBe "groupMember789"
                userFeed.associates[3].type shouldBe ResourceType.STUDY_GROUP_SCHEDULE
                userFeed.associates[3].resourceId shouldBe "schedule101"
                userFeed.associates[4].type shouldBe ResourceType.STUDY_GROUP_POST
                userFeed.associates[4].resourceId shouldBe "post202"
                userFeed.associates[5].type shouldBe ResourceType.STUDY_GROUP_POST_COMMENT
                userFeed.associates[5].resourceId shouldBe "comment303"
            }
        }
    }

    given("UserFeed의 종합적인 동작 테스트") {

        `when`("UserFeed의 모든 기능을 조합하여 사용할 때") {
            val memberId = ObjectId()
            val userFeed = UserFeed(
                memberId = memberId,
                message = "종합 테스트 메시지",
                associates = listOf(
                    AssociatedResource(ResourceType.STUDY_GROUP, "group1")
                ),
                isRead = false
            )

            then("초기 상태가 올바르고 소유자 확인 후 읽음 처리가 정상 작동해야 한다") {
                // 초기 상태 확인
                userFeed.isRead shouldBe false
                userFeed.isOwnedBy(memberId.toHexString()) shouldBe true

                // 읽음 처리
                userFeed.markAsRead()
                userFeed.isRead shouldBe true

                // 여전히 소유자여야 함
                userFeed.isOwnedBy(memberId.toHexString()) shouldBe true

                // 다른 사용자는 소유자가 아님
                val otherMemberId = ObjectId()
                userFeed.isOwnedBy(otherMemberId.toHexString()) shouldBe false
            }
        }
    }
})
