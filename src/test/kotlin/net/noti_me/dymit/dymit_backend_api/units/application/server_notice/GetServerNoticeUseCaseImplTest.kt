package net.noti_me.dymit.dymit_backend_api.units.application.server_notice

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.server_notice.impl.GetNoticeUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.units.domain.server_notice.ServerNoticeTest
import org.bson.types.ObjectId

internal class GetServerNoticeUseCaseImplTest(): BehaviorSpec() {

    private val serverNoticeRepository = mockk<ServerNoticeRepository>()

    private val admin = createMemberEntity(roles = listOf(MemberRole.ROLE_ADMIN))

    private var target = ServerNoticeTest.Companion.createServerNotice(id = ObjectId.get(), writer = admin)

    private val usecase = GetNoticeUseCaseImpl(
        serverNoticeRepository = serverNoticeRepository
    )

    init {
        beforeEach {
            target = ServerNoticeTest.Companion.createServerNotice(id = ObjectId.get(), writer = admin)
        }

        Given("공지의 ID가 주어진다.") {
            When("존재하는 ID로 조회하면") {
                Then("정상적으로 공지를 반환한다.") {
                    val existsId = target.id.toString()
                    every { serverNoticeRepository.findById(any<ObjectId>()) } returns target
                    val result = usecase.getNotice(existsId)
                    result.id.toHexString() shouldBe target.identifier
                }
            }
//
            When("존재하지 않는 ID로 조회하면") {
                val notExistsId = ObjectId.get().toString()
                every { serverNoticeRepository.findById(any<ObjectId>()) } returns null
                Then("에러가 발생한다.") {
                    shouldThrow<NotFoundException> {
                        usecase.getNotice(notExistsId)
                    }
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }
}