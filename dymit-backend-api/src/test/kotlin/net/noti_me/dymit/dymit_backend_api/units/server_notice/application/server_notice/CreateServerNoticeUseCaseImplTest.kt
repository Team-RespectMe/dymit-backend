package net.noti_me.dymit.dymit_backend_api.units.server_notice.application

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.CreateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.server_notice.application.CreateNoticeUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNotice
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.ServerNoticeMemberPort
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.out.persistence.ServerNoticeRepository
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import net.noti_me.dymit.dymit_backend_api.units.server_notice.domain.ServerNoticeTest.Companion.createServerNotice
import net.noti_me.dymit.dymit_backend_api.units.server_notice.domain.ServerNoticeTest.Companion.createServerNoticeMemberDto
import org.bson.types.ObjectId

internal class CreateServerNoticeUseCaseImplTest(
): BehaviorSpec({

    val serverNoticeMemberPort = mockk<ServerNoticeMemberPort>(relaxed = true, relaxUnitFun = true)

    val serverNoticeRepository = mockk<ServerNoticeRepository>(relaxed = true, relaxUnitFun = true)

    val usecase = CreateNoticeUseCaseImpl(
        serverNoticeMemberPort = serverNoticeMemberPort,
        serverNoticeRepository = serverNoticeRepository
    )

    val admin = createMemberEntity(roles = listOf(MemberRole.ROLE_ADMIN))

    val adminInfo = createMemberInfo(admin)

    var notice: ServerNotice = createServerNotice(id = ObjectId.get(), writer = admin)

    beforeEach {
        notice = createServerNotice(id = ObjectId.get(), writer = admin)
//        println("초기화된 notice : ${notice}")
    }

    given("정상 공지 생성 요청이 주어진다.") {
        val command = CreateServerNoticeCommand(
            category = "일반",
            title = "공지 제목",
            content = "공지 내용"
        )
        `when`("정상적인 관리자면") {
            every { serverNoticeMemberPort.loadById(adminInfo.memberId) } returns createServerNoticeMemberDto(admin)
            every { serverNoticeRepository.save(any()) } returns notice

            then("정상적으로 생성된다.") {
                shouldNotThrowAny {
                    usecase.execute(adminInfo, command)
                }
            }
        }

        `when`("회원 정보 조회가 실패하면") {
            every { serverNoticeMemberPort.loadById(any<String>())} returns null

            then("예외가 발생한다.") {
                shouldThrow< ForbiddenException > {
                    usecase.execute(adminInfo, command)
                }
            }
        }
    }

    afterEach {
        clearAllMocks()
    }
})
