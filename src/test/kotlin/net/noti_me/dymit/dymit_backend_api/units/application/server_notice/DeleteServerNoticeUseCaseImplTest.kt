package net.noti_me.dymit.dymit_backend_api.units.application.server_notice

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.server_notice.impl.DeleteNoticeUseCaseImpl
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

internal class DeleteServerNoticeUseCaseImplTest : BehaviorSpec({

    val loadMemberPort = mockk<LoadMemberPort>()

    val serverNoticeRepository = mockk<ServerNoticeRepository>()

    val usecase = DeleteNoticeUseCaseImpl(
        loadMemberPort = loadMemberPort,
        serverNoticeRepository = serverNoticeRepository
    )

    val member = createMemberEntity()

    val memberInfo = createMemberInfo(member)

    val admin = createMemberEntity(roles = listOf(MemberRole.ROLE_ADMIN))

    val adminInfo = createMemberInfo(admin)

    var target: ServerNotice = createServerNotice(writer = admin, id = ObjectId.get())

    beforeEach {
        target = createServerNotice(writer = admin, id = ObjectId.get())
    }

    given("삭제할 공지의 ID가 주어진다.") {
        `when`("관리자가 아니라면") {
            every { loadMemberPort.loadById(any<String>()) } returns member
            every  { serverNoticeRepository.findById(any()) }     returns target
            then("삭제에 실패한다.") {
                shouldThrow< ForbiddenException > {
                    usecase.delete(memberInfo, target.id.toString())
                }
            }
        }

        `when`("관리자라면") {
            every { loadMemberPort.loadById(any<String>()) } returns admin
            every  { serverNoticeRepository.findById(any()) } returns target
            every { serverNoticeRepository.delete(any()) } returns Unit
            then("정상적으로 삭제된다.") {
                shouldNotThrowAny {
                    usecase.delete(adminInfo, target.id.toString())
                }
            }
        }
    }

    afterEach {
        clearAllMocks()
    }
}) {
}