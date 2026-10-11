package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetManagedTasksQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.ManagedTaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.ManagedTaskGroupDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.out.persistence.ManagedTaskQueryPort
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetManagedTasksUseCase
import org.springframework.stereotype.Service
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 소유 그룹의 기준일 마감 과제를 조회하고 미제출 대상자 수를 결합합니다. */
@Service
class GetManagedTasksUseCaseImpl(
    private val queryPort: ManagedTaskQueryPort
) : GetManagedTasksUseCase {

    override fun execute(query: GetManagedTasksQuery): List<ManagedTaskDto> {
        validateSize(query.size)
        val ownerId = TaskUseCaseObjectIdParser.parse(query.memberInfo.memberId, "memberId")
        val cursor = query.cursor?.let(::parseCursor)
        val queryDate = cursor?.date ?: query.today
        val (startInclusive, endExclusive) = createQueryWindow(queryDate)
        if (cursor != null && (cursor.expireAt < startInclusive || cursor.expireAt >= endExclusive)) {
            throw BadRequestException(message = "커서의 마감 시각이 조회 날짜 범위를 벗어났습니다.")
        }
        val tasks = queryPort.load(
            ownerId = ownerId,
            startInclusive = startInclusive,
            endExclusive = endExclusive,
            cursor = cursor,
            limit = query.size + 1
        )
        val counts = queryPort.countNotSubmittedAssignees(tasks.map { it.taskId })
        return tasks.map { task ->
            ManagedTaskDto(
                group = ManagedTaskGroupDto(
                    id = task.groupId.toHexString(),
                    name = task.groupName
                ),
                taskId = task.taskId.toHexString(),
                title = task.title,
                expireAt = task.expireAt,
                notSubmittedAssigneeCount = counts[task.taskId] ?: 0L,
                cursor = ManagedTaskCursor(queryDate, task.expireAt, task.taskId).encode()
            )
        }
    }

    private fun parseCursor(value: String): ManagedTaskCursor {
        return try {
            ManagedTaskCursor.decode(value)
        } catch (exception: IllegalArgumentException) {
            throw BadRequestException(message = "올바르지 않은 cursor입니다.")
        }
    }

    private fun validateSize(size: Int) {
        if (size !in 1..MAX_PAGE_SIZE) {
            throw BadRequestException(message = "size는 1 이상 $MAX_PAGE_SIZE 이하여야 합니다.")
        }
    }

    private fun createQueryWindow(date: LocalDate): Pair<Instant, Instant> {
        return try {
            val startInclusive = date.atStartOfDay(KOREA_ZONE).toInstant()
            val endExclusive = date.plusDays(1).atStartOfDay(KOREA_ZONE).toInstant()
            startInclusive.toEpochMilli()
            endExclusive.toEpochMilli()
            startInclusive to endExclusive
        } catch (exception: DateTimeException) {
            throw invalidCursorDate()
        } catch (exception: ArithmeticException) {
            throw invalidCursorDate()
        }
    }

    private fun invalidCursorDate(): BadRequestException {
        return BadRequestException(message = "cursor의 조회 날짜로 기간을 계산할 수 없습니다.")
    }

    private companion object {
        const val MAX_PAGE_SIZE = 100
        val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
