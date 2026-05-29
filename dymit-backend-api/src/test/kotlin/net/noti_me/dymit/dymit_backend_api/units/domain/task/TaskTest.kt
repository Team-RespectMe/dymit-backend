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
                        expireAt = LocalDateTime.now().plusHours(25)
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
                        expireAt = LocalDateTime.now().plusHours(25)
                    )
                }

                exception.message shouldBe "과제 설명은 4000자 이하로 작성해야 합니다."
            }
        }

        `when`("마감 시각이 현재 시각 기준 24시간 이전이면") {
            then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    createTask(
                        attachments = emptyList(),
                        description = "설명",
                        expireAt = LocalDateTime.now().plusHours(23)
                    )
                }

                exception.message shouldBe "마감일은 현재 시각 기준 24시간 이후여야 합니다."
            }
        }

        `when`("첨부 5개와 24시간 이상 마감 시각을 사용하면") {
            then("정상 생성된다") {
                val attachments = List(5) { TaskAttachment(fileId = ObjectId.get()) }

                val task = createTask(
                    attachments = attachments,
                    description = "정상 설명",
                    expireAt = LocalDateTime.now().plusHours(25)
                )

                task.attachments.size shouldBe 5
                task.description shouldBe "정상 설명"
            }
        }
    }
})
