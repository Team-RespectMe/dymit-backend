package net.noti_me.dymit.dymit_backend_api.units.task.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAttachment
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class TaskTest : BehaviorSpec({

    fun createTask(
        attachments: List<TaskAttachment>,
        description: String,
        expireAt: LocalDateTime,
        submissionType: TaskSubmissionType = TaskSubmissionType.OUTPUT
    ): Task {
        return Task(
            relatedScheduleId = ObjectId.get(),
            type = TaskType.PRE,
            title = "과제 제목",
            description = description,
            attachments = attachments,
            expireAt = expireAt,
            submissionType = submissionType
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

        `when`("submissionType을 CHECK로 생성하면") {
            then("도메인에 그대로 보존된다") {
                val task = createTask(
                    attachments = emptyList(),
                    description = "정상 설명",
                    expireAt = LocalDateTime.now().plusHours(1),
                    submissionType = TaskSubmissionType.CHECK
                )

                task.submissionType shouldBe TaskSubmissionType.CHECK
            }
        }

        `when`("과제를 수정해도") {
            then("submissionType은 변경되지 않는다") {
                val task = createTask(
                    attachments = emptyList(),
                    description = "정상 설명",
                    expireAt = LocalDateTime.now().plusHours(1),
                    submissionType = TaskSubmissionType.CHECK
                )

                task.update(
                    title = "수정 제목",
                    description = "수정 설명",
                    attachments = emptyList(),
                    expireAt = LocalDateTime.now().plusHours(2)
                )

                task.submissionType shouldBe TaskSubmissionType.CHECK
            }
        }
    }
})
