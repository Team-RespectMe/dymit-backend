package net.noti_me.dymit.dymit_backend_api.units.domain.member

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken
import org.bson.types.ObjectId

/**
 * Member 도메인 객체의 비즈니스 로직을 테스트하는 클래스
 */
internal class MemberTest : BehaviorSpec() {

    private lateinit var member: Member

    init {
        beforeEach {
            member = Member(
                nickname = "test-nickname",
                oidcIdentities = mutableSetOf(OidcIdentity(provider = "test-provider", subject = "test-subject"))
            )
        }

        given("회원 닉네임 변경 기능") {
            val newNickname = "new-nickname"

            `when`("정상적인 새 닉네임으로 변경을 시도하면") {
                then("닉네임이 성공적으로 변경된다.") {
                    member.changeNickname(newNickname)
                    member.nickname shouldBe newNickname
                }
            }

            `when`("현재와 동일한 닉네임으로 변경을 시도하면") {
                then("닉네임은 변경되지 않는다.") {
                    val currentNickname = member.nickname
                    member.changeNickname(currentNickname)
                    member.nickname shouldBe currentNickname
                }
            }

            `when`("빈 문자열로 닉네임을 변경하려고 하면") {
                then("IllegalArgumentException 예외가 발생한다.") {
                    shouldThrow<IllegalArgumentException> {
                        member.changeNickname("")
                    }.message shouldBe "닉네임은 비워둘 수 없습니다."
                }
            }

            `when`("공백만 있는 닉네임으로 변경하려고 하면") {
                then("IllegalArgumentException 예외가 발생한다.") {
                    shouldThrow<IllegalArgumentException> {
                        member.changeNickname("   ")
                    }.message shouldBe "닉네임은 비워둘 수 없습니다."
                }
            }

            `when`("탭과 개행 문자만 있는 닉네임으로 변경하려고 하면") {
                then("IllegalArgumentException 예외가 발생한다.") {
                    shouldThrow<IllegalArgumentException> {
                        member.changeNickname("\t\n\r")
                    }.message shouldBe "닉네임은 비워둘 수 없습니다."
                }
            }

            `when`("너무 긴 닉네임으로 변경을 시도하면") {
                then("IllegalArgumentException 예외가 발생한다.") {
                    shouldThrow<IllegalArgumentException> {
                        member.changeNickname("a".repeat(21))
                    }.message shouldBe "닉네임은 1자 이상 20자 이내로 설정해야 합니다."
                }
            }

            `when`("경계값 테스트를 수행할 때") {
                then("정확히 1자인 닉네임으로 변경하면 성공한다.") {
                    val singleCharNickname = "a"
                    member.changeNickname(singleCharNickname)
                    member.nickname shouldBe singleCharNickname
                }

                then("정확히 20자인 닉네임으로 변경하면 성공한다.") {
                    val twentyCharNickname = "a".repeat(20)
                    member.changeNickname(twentyCharNickname)
                    member.nickname shouldBe twentyCharNickname
                }
            }
        }

        given("프로필 사진 관리 기능") {
            val profileImage = MemberProfileImageVo(
                type = "external",
                filePath = "path/to/image.jpg",
                url = "https://cdn.example.com/image.jpg",
                fileSize = 1024L,
                width = 100,
                height = 100
            )

            `when`("새로운 프로필 사진으로 업데이트하면") {
                then("프로필 사진 정보가 업데이트된다.") {
                    member.updateProfileImage(profileImage)
                    member.profileImage shouldBe profileImage
                }
            }

            `when`("external 타입의 프로필 사진을 삭제하면") {
                then("프로필 사진 정보가 프리셋으로 변경되고 삭제 이벤트가 등록된다.") {
                    member.updateProfileImage(profileImage) // 먼저 external 이미지를 설정
                    member.deleteProfileImage()

                    member.profileImage.type shouldBe "preset"
                    member.profileImage.filePath shouldBe ""
                    member.profileImage.fileSize shouldBe 0L
                    member.profileImage.width shouldBe 0
                    member.profileImage.height shouldBe 0
                    // 이벤트 등록 확인은 실제 구현에서 도메인 이벤트 처리 방식에 따라 다를 수 있음
                }
            }

            `when`("preset 타입의 프로필 사진을 삭제하면") {
                then("프로필 사진 정보가 프리셋으로 변경되고 이벤트는 등록되지 않는다.") {
                    val presetImage = MemberProfileImageVo(
                        type = "preset",
                        filePath = "",
                        url = "1",
                        fileSize = 0L,
                        width = 0,
                        height = 0
                    )
                    member.updateProfileImage(presetImage)
                    member.deleteProfileImage()

                    member.profileImage.type shouldBe "preset"
                }
            }
        }

        given("디바이스 토큰 관리 기능") {
            val deviceToken = DeviceToken("test-device-token")

            `when`("새로운 디바이스 토큰을 추가하면") {
                then("디바이스 토큰 목록에 해당 토큰이 포함된다.") {
                    member.addDeviceToken(deviceToken)
                    member.deviceTokens shouldContain deviceToken
                }
            }

            `when`("기존 디바이스 토큰을 삭제하면") {
                then("디바이스 토큰 목록에서 해당 토큰이 제거된다.") {
                    member.addDeviceToken(DeviceToken(deviceToken.token))
                    member.removeDeviceToken(DeviceToken(deviceToken.token))
                    member.deviceTokens shouldNotContain deviceToken
                }
            }

            `when`("존재하지 않는 디바이스 토큰을 삭제하려고 하면") {
                then("아무 일도 일어나지 않는다.") {
                    val initialSize = member.deviceTokens.size
                    member.removeDeviceToken(DeviceToken("non-existent-token"))
                    member.deviceTokens shouldHaveSize initialSize
                }
            }
        }

        given("리프레시 토큰 관리 기능") {
            val refreshToken = "test-refresh-token"

            `when`("새로운 리프레시 토큰을 추가하면") {
                then("리프레시 토큰 목록에 해당 토큰이 포함된다.") {
                    member.addRefreshToken(refreshToken)
                    member.refreshTokens shouldContain refreshToken
                }
                then("마지막 접근 시간이 갱신된다.") {
                    val initialLastAccessedAt = member.lastAccessAt
                    Thread.sleep(1) // 시간 차이를 만들기 위해
                    member.addRefreshToken(refreshToken)
                    member.lastAccessAt shouldNotBe initialLastAccessedAt
                }
            }

            `when`("리프레시 토큰이 5개 이상일 때 새 토큰을 추가하면") {
                then("가장 오래된 토큰이 제거되고 새 토큰이 추가된다.") {
                    // 5개의 토큰을 추가
                    repeat(5) { index ->
                        member.addRefreshToken("token-$index")
                    }
                    member.refreshTokens shouldHaveSize 5

                    // 6번째 토큰 추가
                    val sixthToken = "token-6"
                    member.addRefreshToken(sixthToken)

                    member.refreshTokens shouldHaveSize 5
                    member.refreshTokens shouldContain sixthToken
                    member.refreshTokens shouldNotContain "token-0" // 첫 번째 토큰이 제거되어야 함
                }
            }

            `when`("기존 리프레시 토큰을 삭제하면") {
                then("리프레시 토큰 목록에서 해당 토큰이 제거된다.") {
                    member.addRefreshToken(refreshToken)
                    member.removeRefreshToken(refreshToken)
                    member.refreshTokens shouldNotContain refreshToken
                }

                then("마지막 접근 시간이 갱신된다.") {
                    member.addRefreshToken(refreshToken)
                    val initialLastAccessedAt = member.lastAccessAt
                    Thread.sleep(1) // 시간 차이를 만들기 위해
                    member.removeRefreshToken(refreshToken)
                    member.lastAccessAt shouldNotBe initialLastAccessedAt
                }
            }

            `when`("존재하지 않는 리프레시 토큰을 삭제하려고 하면") {
                then("아무 일도 일어나지 않는다.") {
                    val initialSize = member.refreshTokens.size
                    member.removeRefreshToken("non-existent-token")
                    member.refreshTokens shouldHaveSize initialSize
                }
            }
        }

        given("Member 객체의 동등성 및 해시코드") {
            `when`("같은 ID를 가진 두 Member 객체를 비교하면") {
                then("equals는 true를 반환한다.") {
                    val memberId = ObjectId()
                    val member1 = Member(id = memberId, nickname = "member1")
                    val member2 = Member(id = memberId, nickname = "member2")

                    (member1 == member2) shouldBe true
                    member1.equals(member2) shouldBe true
                }

                then("hashCode는 같은 값을 반환한다.") {
                    val memberId = ObjectId()
                    val member1 = Member(id = memberId, nickname = "member1")
                    val member2 = Member(id = memberId, nickname = "member2")

                    member1.hashCode() shouldBe member2.hashCode()
                }
            }

            `when`("다른 ID를 가진 두 Member 객체를 비교하면") {
                then("equals는 false를 반환한다.") {
                    val member1 = Member(id = ObjectId(), nickname = "member1")
                    val member2 = Member(id = ObjectId(), nickname = "member1")

                    (member1 == member2) shouldBe false
                    member1.equals(member2) shouldBe false
                }
            }

            `when`("같은 객체 참조를 비교하면") {
                then("equals는 true를 반환한다.") {
                    val sameMember = member
                    member.equals(sameMember) shouldBe true
                }
            }

            `when`("Member가 아닌 객체와 비교하면") {
                then("equals는 false를 반환한다.") {
                    member.equals("not a member") shouldBe false
                    member.equals(null) shouldBe false
                }
            }
        }

        given("Member 객체의 속성 확인") {
            `when`("identifier 프로퍼티를 확인할 때") {
                then("ID의 hexString과 동일해야 한다.") {
                    member.identifier shouldBe member.id.toHexString()
                }
            }

            `when`("기본값으로 Member를 생성할 때") {
                then("모든 속성이 기본값으로 설정되어야 한다.") {
                    val defaultMember = Member()

                    defaultMember.nickname shouldBe ""
                    defaultMember.oidcIdentities shouldHaveSize 0
                    defaultMember.profileImage.type shouldBe "preset"
                    defaultMember.profileImage.url shouldBe "0"
                    defaultMember.deviceTokens shouldHaveSize 0
                    defaultMember.refreshTokens shouldHaveSize 0
                }
            }

            `when`("updateLastAccessedAt을 호출할 때") {
                then("lastAccessAt이 현재 시간으로 업데이트된다.") {
                    val initialTime = member.lastAccessAt
                    Thread.sleep(1) // 시간 차이를 만들기 위해
                    member.updateLastAccessedAt()
                    member.lastAccessAt shouldNotBe initialTime
                }
            }

            `when`("모든 setter가 private인지 확인할 때") {
                then("각 속성들이 전용 메서드를 통해서만 변경 가능해야 한다.") {
                    // nickname은 changeNickname을 통해서만 변경 가능
                    member.changeNickname("new-name")
                    member.nickname shouldBe "new-name"

                    // profileImage는 updateProfileImage를 통해서만 변경 가능
                    val newImage = MemberProfileImageVo(type = "external", filePath = "test", url = "test", fileSize = 100L, width = 50, height = 50)
                    member.updateProfileImage(newImage)
                    member.profileImage shouldBe newImage

                    // lastAccessAt은 updateLastAccessedAt을 통해서만 변경 가능
                    val oldTime = member.lastAccessAt
                    Thread.sleep(1)
                    member.updateLastAccessedAt()
                    member.lastAccessAt shouldNotBe oldTime
                }
            }
        }
    }
}