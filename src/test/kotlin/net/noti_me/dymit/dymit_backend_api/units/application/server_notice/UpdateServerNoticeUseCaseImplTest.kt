package net.noti_me.dymit.dymit_backend_api.units.application.server_notice

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.scopes.BehaviorSpecGivenContainerScope
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.UpdateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.application.server_notice.impl.DeleteNoticeUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.server_notice.impl.UpdateNoticeUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import net.noti_me.dymit.dymit_backend_api.units.domain.server_notice.ServerNoticeTest.Companion.createServerNotice
import org.bson.types.ObjectId

internal class UpdateServerNoticeUseCaseImplTest : BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val serverNoticeRepository = mockk<ServerNoticeRepository>()

    private val usecase = UpdateNoticeUseCaseImpl(
        loadMemberPort = loadMemberPort,
        serverNoticeRepository = serverNoticeRepository
    )

    private lateinit var target: ServerNotice

    private val admin = createMemberEntity(roles = listOf(MemberRole.ROLE_ADMIN))

    private val adminInfo = createMemberInfo(admin)

    private val member = createMemberEntity()

    private val memberInfo = createMemberInfo(member)

    init {

        target =  createServerNotice(writer = admin, id = ObjectId.get())

        beforeEach() {
            target = createServerNotice(writer = admin, id = ObjectId.get())
        }

        Given("업데이트 할 공지가 주어진다.") {
            val command = UpdateServerNoticeCommand(
                noticeId = target.id!!,
                title = "업데이트된 제목",
                content = "업데이트된 내용"
            )

            When("관리자라면") {
                every { loadMemberPort.loadById(any<String>()) } returns admin
                every { serverNoticeRepository.findById(any()) } returns target
                every { serverNoticeRepository.save(any()) } returns target

                Then("업데이트 된다.") {
                    val updated = usecase.update(adminInfo, command)
                    updated.title shouldBe  command.title
                    updated.content shouldBe command.content
                }
            }

            When("관리자가 아니라면") {
                every { loadMemberPort.loadById(any<String>()) } returns member
                every  { serverNoticeRepository.findById(any()) }     returns target

                Then("업데이트에 실패한다.") {
                    shouldThrow<ForbiddenException> {
                        usecase.update(memberInfo, command)
                    }
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }
}