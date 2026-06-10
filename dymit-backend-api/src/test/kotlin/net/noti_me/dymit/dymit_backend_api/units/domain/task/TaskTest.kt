package net.noti_me.dymit.dymit_backend_api.units.domain.task

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAttachment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class TaskTest : BehaviorSpec({

    fun createTask(attachments: List<TaskAttachment>, description: String, expireAt: LocalDateTime): Task {
        return Task(
            relatedScheduleId = ObjectId.get(),
            type = TaskType.PRE,
            title = "과제 제목",
            description = description,
            attachments = attachments,
            expireAt = expireAt
        )
    }

    given("과제 도메인 유효성 검증") {
        `when`("첨부 개수가 6개이면") {
            then("BadRequestException이 발생한다") {
                val attachments = List(6) { TaskAttachment(fileId = ObjectId.get()) }

                val exception = shouldThrow<BadRequestException> {
                    createTask(
                        attachments = attachments,
                        description = "설명",
                        expireAt = LocalDateTime.now().plusHours(1)
                    )
                }

                exception.message shouldBe "과제 첨부 파일은 최대 5개까지 가능합니다."
            }
        }

        `when`("설명 길이가 4001자이면") {
            then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    createTask(
                        attachments = emptyList(),
                        description = "a".repeat(4001),
                        expireAt = LocalDateTime.now().plusHours(1)
                    )
                }

                exception.message shouldBe "과제 설명은 4000자 이하로 작성해야 합니다."
            }
        }

        `when`("마감 시각이 이미 지난 값이어도") {
            then("도메인 생성은 허용된다") {
                val task = createTask(
                    attachments = emptyList(),
                    description = "정상 설명",
                    expireAt = LocalDateTime.now().minusHours(1)
                )

                task.expireAt.isBefore(LocalDateTime.now()) shouldBe true
            }
        }
    }
})
