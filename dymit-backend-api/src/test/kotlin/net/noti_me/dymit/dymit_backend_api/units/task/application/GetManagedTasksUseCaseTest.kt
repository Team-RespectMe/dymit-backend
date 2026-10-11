package net.noti_me.dymit.dymit_backend_api.units.task.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import net.noti_me.dymit.dymit_backend_api.task.application.*
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetManagedTasksQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.*
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDate
import java.util.Base64

internal class GetManagedTasksUseCaseTest : BehaviorSpec({
    val date = LocalDate.of(2026, 10, 11)
    val start = Instant.parse("2026-10-10T15:00:00Z")
    val end = Instant.parse("2026-10-11T15:00:00Z")
    val member = createMemberInfo(createMemberEntity())
    listOf(1, 20, 100).forEach { size ->
        Given("한국 날짜 조회 size $size") {
            Then("오늘 상하한과 소유자 ID로 size+1개를 조회하고 대상자 수를 일괄 결합한다") {
                val port = mockk<ManagedTaskQueryPort>()
                val ids = List(2) { ObjectId() }
                val rows = ids.mapIndexed { i, id -> ManagedTaskQueryResult(id, "과제 $i", if(i == 0) start else end.minusMillis(1), ObjectId(), "현재 그룹 $i") }
                every { port.load(ObjectId(member.memberId), start, end, null, size + 1) } returns rows
                every { port.countNotSubmittedAssignees(ids) } returns mapOf(ids[0] to 7L)
                val result = GetManagedTasksUseCaseImpl(port).execute(GetManagedTasksQuery(member, size = size, today = date))
                result.map { it.taskId } shouldBe ids.map { it.toHexString() }
                result.map { it.notSubmittedAssigneeCount } shouldBe listOf(7L, 0L)
                result.map { it.group.name } shouldBe listOf("현재 그룹 0", "현재 그룹 1")
                result.forEachIndexed { i, item -> ManagedTaskCursor.decode(item.cursor) shouldBe ManagedTaskCursor(date, rows[i].expireAt, ids[i]) }
                verify(exactly = 1) { port.countNotSubmittedAssignees(ids) }
                verify(exactly = 1) { port.load(ObjectId(member.memberId), start, end, null, size + 1) }
                confirmVerified(port)
            }
        }
    }
    Given("자정을 넘어 이어지는 조회") {
        Then("커서의 최초 날짜와 동일 마감시각의 ID 위치를 유지한다") {
            val port = mockk<ManagedTaskQueryPort>()
            val cursor = ManagedTaskCursor(date, start.plusSeconds(100), ObjectId())
            every { port.load(ObjectId(member.memberId), start, end, cursor, 21) } returns emptyList()
            every { port.countNotSubmittedAssignees(emptyList()) } returns emptyMap()
            GetManagedTasksUseCaseImpl(port).execute(GetManagedTasksQuery(member, cursor.encode(), today = date.plusDays(1))) shouldBe emptyList()
            verify(exactly = 1) { port.load(ObjectId(member.memberId), start, end, cursor, 21) }
        }
    }
    listOf(-1, 0, 101).forEach { size ->
        Given("허용 범위 밖 size $size") {
            Then("외부 조회 없이 400 오류를 반환한다") {
                val port = mockk<ManagedTaskQueryPort>()
                shouldThrow<BadRequestException> { GetManagedTasksUseCaseImpl(port).execute(GetManagedTasksQuery(member, size = size, today = date)) }
                verify { port wasNot Called }
            }
        }
    }
    fun encoded(payload: String) = "v1." + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
    val invalid = listOf(
        "", "v2.abc", "v1.!",
        encoded("bad:0:${ObjectId()}"),
        encoded("$date:bad:${ObjectId()}"),
        encoded("$date:0:bad"),
        encoded("$date:0:${ObjectId()}:extra"),
        encoded("+999999999-12-31:0:${ObjectId()}"),
        encoded("+500000000-01-01:0:${ObjectId()}"),
        ManagedTaskCursor(date, start.minusMillis(1), ObjectId()).encode(),
        ManagedTaskCursor(date, end, ObjectId()).encode()
    )
    invalid.forEachIndexed { index, value ->
        Given("손상되거나 날짜 범위를 벗어난 커서 $index") {
            Then("포트 호출 전에 400 오류를 반환한다") {
                val port = mockk<ManagedTaskQueryPort>()
                shouldThrow<BadRequestException> { GetManagedTasksUseCaseImpl(port).execute(GetManagedTasksQuery(member, value, today = date)) }
                verify { port wasNot Called }
            }
        }
    }
    Given("밀리초 마감시각 커서") {
        Then("날짜와 시각 및 ID를 손실 없이 왕복하고 URL 안전한 문자열을 만든다") {
            val cursor = ManagedTaskCursor(date, start.plusMillis(12345), ObjectId())
            ManagedTaskCursor.decode(cursor.encode()) shouldBe cursor
            cursor.encode().contains("=") shouldBe false
            cursor.encode().contains("+") shouldBe false
            cursor.encode().contains("/") shouldBe false
        }
    }
})
