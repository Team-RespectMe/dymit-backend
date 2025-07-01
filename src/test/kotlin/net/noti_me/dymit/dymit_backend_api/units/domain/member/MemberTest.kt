package net.noti_me.dymit.dymit_backend_api.units.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken

internal class MemberTest : BehaviorSpec() {

    private lateinit var member: Member

    init {
        beforeEach {
            member = Member(
                nickname = "test-nickname",
                oidcIdentity = OidcIdentity(provider = "test-provider", subject = "test-subject"),
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

            `when`("너무 짧은 닉네임으로 변경을 시도하면") {
                then("IllegalArgumentException 예외가 발생한다.") {
                    shouldThrow<IllegalArgumentException> {
                        member.changeNickname("a")
                    }
                }
            }

            `when`("너무 긴 닉네임으로 변경을 시도하면") {
                then("IllegalArgumentException 예외가 발생한다.") {
                    shouldThrow<IllegalArgumentException> {
                        member.changeNickname("a".repeat(21))
                    }
                }
            }
        }

        given("프로필 사진 관리 기능") {
            val profileImage = MemberProfileImageVo(
                filePath = "path/to/image.jpg",
                cdnUrl = "https://cdn.example.com/image.jpg",
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

            `when`("프로필 사진을 삭제하면") {
                then("프로필 사진 정보가 null이 된다.") {
                    member.updateProfileImage(profileImage) // 먼저 이미지를 설정
                    member.deleteProfileImage()
                    member.profileImage shouldBe null
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
        }

        given("리프레시 토큰 관리 기능") {
            val refreshToken = "test-refresh-token"

            `when`("새로운 리프레시 토큰을 추가하면") {
                then("리프레시 토큰 목록에 해당 토큰이 포함된다.") {
                    member.addRefreshToken(refreshToken)
                    member.refreshTokens shouldContain refreshToken
                }
                then("마지막 접근 시간이 갱신된다.") {
                    val initialLastAccessedAt = member.lastAccessedAt
                    member.addRefreshToken(refreshToken)
                    member.lastAccessedAt shouldNotBe initialLastAccessedAt
                }
            }

            `when`("기존 리프레시 토큰을 삭제하면") {
                then("리프레시 토큰 목록에서 해당 토큰이 제거된다.") {
                    member.addRefreshToken(refreshToken)
                    member.removeRefreshToken(refreshToken)
                    member.refreshTokens shouldNotContain refreshToken
                }
            }
        }
    }
}