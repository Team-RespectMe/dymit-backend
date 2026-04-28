package net.noti_me.dymit.dymit_backend_api.units.application.server_notice

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.server_notice.impl.GetNoticesUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.units.domain.server_notice.ServerNoticeTest.Companion.createServerNotice
import org.bson.types.ObjectId

internal class GetServerNoticesUseCaseImplTest(): BehaviorSpec() {

    private val serverNoticeRepository = mockk<ServerNoticeRepository>()

    private val usecase = GetNoticesUseCaseImpl(
        serverNoticeRepository = serverNoticeRepository
    )

    private val defaultItems: MutableList<ServerNotice> = mutableListOf()

    private val admin = createMemberEntity(roles = listOf(MemberRole.ROLE_ADMIN))

    init {
        defaultItems.clear()
        val temp = mutableListOf<ServerNotice>()
        for (i in 1..25) {
            val notice = createServerNotice(
                id = ObjectId.get(),
                writer = admin,
                title = "공지 제목 $i",
                content = "공지 내용 $i"
            )
            temp.add(notice)
        }
        defaultItems.addAll(temp.sortedByDescending { it.id })
        defaultItems.forEach { println(it) }

        Given("공지들을 조회할 때") {
            When("cursor 없이 size만 주어지면") {
                val size = 10
                val cursor = null
                every { serverNoticeRepository.findAllByCursorIdOrderByIdDesc(cursor, size) } returns defaultItems.take(size)

                Then("처음부터 조회된다") {
                    val results = usecase.getNotices(cursor, size)
                    results.size shouldBe size
                    results.forEachIndexed { index, notice ->
                        notice.id shouldBe defaultItems[index].identifier
                    }
                }
            }

            When("커서를 주고 조회를 하면") {
                val cursor = defaultItems[9].id!!
                val size = 10
                every { serverNoticeRepository.findAllByCursorIdOrderByIdDesc(cursor, size) } returns defaultItems.drop(10).take(size)
                Then("커서 직후 부터 조회되어야 한다.") {
                    val results = usecase.getNotices(cursor.toHexString(), size)
                    results.size shouldBe size
                    results.forEachIndexed { index, notice ->
                        notice.id shouldBe defaultItems[index + 10].identifier
                    }
                }
            }
        }


        afterEach {
            clearAllMocks()
        }
    }
}