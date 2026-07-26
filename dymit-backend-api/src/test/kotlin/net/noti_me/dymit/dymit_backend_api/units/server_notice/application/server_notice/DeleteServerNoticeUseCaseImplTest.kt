package net.noti_me.dymit.dymit_backend_api.units.server_notice.application

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.server_notice.application.DeleteNoticeUseCaseImpl
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

internal class DeleteServerNoticeUseCaseImplTest : BehaviorSpec({

    val serverNoticeMemberPort = mockk<ServerNoticeMemberPort>()

    val serverNoticeRepository = mockk<ServerNoticeRepository>()

    val usecase = DeleteNoticeUseCaseImpl(
        serverNoticeMemberPort = serverNoticeMemberPort,
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
            every { serverNoticeMemberPort.loadById(any<String>()) } returns createServerNoticeMemberDto(member)
            every  { serverNoticeRepository.findById(any()) }     returns target
            then("삭제에 실패한다.") {
                shouldThrow< ForbiddenException > {
                    usecase.execute(memberInfo, target.id.toString())
                }
            }
        }

        `when`("관리자라면") {
            every { serverNoticeMemberPort.loadById(any<String>()) } returns createServerNoticeMemberDto(admin)
            every  { serverNoticeRepository.findById(any()) } returns target
            every { serverNoticeRepository.delete(any()) } returns Unit
            then("정상적으로 삭제된다.") {
                shouldNotThrowAny {
                    usecase.execute(adminInfo, target.id.toString())
                }
            }
        }
    }

    afterEach {
        clearAllMocks()
    }
}) {
}
