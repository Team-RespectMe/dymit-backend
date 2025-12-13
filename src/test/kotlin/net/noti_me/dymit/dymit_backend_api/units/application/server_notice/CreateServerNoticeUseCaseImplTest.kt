package net.noti_me.dymit.dymit_backend_api.units.application.server_notice

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.CreateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.application.server_notice.impl.CreateNoticeUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import net.noti_me.dymit.dymit_backend_api.units.domain.server_notice.ServerNoticeTest.Companion.createServerNotice
import org.bson.types.ObjectId

internal class CreateServerNoticeUseCaseImplTest(
): BehaviorSpec({

    val loadMemberPort = mockk<LoadMemberPort>(relaxed = true, relaxUnitFun = true)

    val serverNoticeRepository = mockk<ServerNoticeRepository>(relaxed = true, relaxUnitFun = true)

    val usecase = CreateNoticeUseCaseImpl(
        loadMemberPort = loadMemberPort,
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
            title = "공지 제목",
            content = "공지 내용"
        )
        `when`("정상적인 관리자면") {
            every { loadMemberPort.loadById(adminInfo.memberId) } returns admin
            every { serverNoticeRepository.save(any()) } returns notice

            then("정상적으로 생성된다.") {
                shouldNotThrowAny {
                    usecase.create(adminInfo, command)
                }
            }
        }

        `when`("회원 정보 조회가 실패하면") {
            every { loadMemberPort.loadById(any<String>())} returns null

            then("예외가 발생한다.") {
                shouldThrow< ForbiddenException > {
                    usecase.create(adminInfo, command)
                }
            }
        }
    }

    afterEach {
        clearAllMocks()
    }
})
