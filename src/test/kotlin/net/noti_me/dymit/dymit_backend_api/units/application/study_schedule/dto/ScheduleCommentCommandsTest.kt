package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule.dto

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.CreateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.UpdateScheduleCommentCommand
import org.bson.types.ObjectId

/**
 * ScheduleCommentCommands 테스트 클래스
 * 스케줄 댓글 명령 DTO들의 생성자와 속성을 테스트합니다.
 */
class ScheduleCommentCommandsTest : AnnotationSpec() {

    @Test
    fun `CreateScheduleCommentCommand를 정상적으로 생성할 수 있다`() {
        // Given
        val groupId = ObjectId.get()
        val scheduleId = ObjectId.get()
        val content = "테스트 댓글 내용"

        // When
        val command = CreateScheduleCommentCommand(
            groupId = groupId,
            scheduleId = scheduleId,
            content = content
        )

        // Then
        command.groupId shouldBe groupId
        command.scheduleId shouldBe scheduleId
        command.content shouldBe content
    }

    @Test
    fun `UpdateScheduleCommentCommand를 정상적으로 생성할 수 있다`() {
        // Given
        val groupId = ObjectId.get()
        val scheduleId = ObjectId.get()
        val commentId = ObjectId.get()
        val content = "수정된 댓글 내용"

        // When
        val command = UpdateScheduleCommentCommand(
            groupId = groupId,
            scheduleId = scheduleId,
            commentId = commentId,
            content = content
        )

        // Then
        command.groupId shouldBe groupId
        command.scheduleId shouldBe scheduleId
        command.commentId shouldBe commentId
        command.content shouldBe content
    }

    @Test
    fun `CreateScheduleCommentCommand에 빈 내용으로 생성할 수 있다`() {
        // Given
        val groupId = ObjectId.get()
        val scheduleId = ObjectId.get()
        val emptyContent = ""

        // When
        val command = CreateScheduleCommentCommand(
            groupId = groupId,
            scheduleId = scheduleId,
            content = emptyContent
        )

        // Then
        command.content shouldBe emptyContent
    }

    @Test
    fun `UpdateScheduleCommentCommand에 긴 내용으로 생성할 수 있다`() {
        // Given
        val groupId = ObjectId.get()
        val scheduleId = ObjectId.get()
        val commentId = ObjectId.get()
        val longContent = "이것은 매우 긴 댓글 내용입니다. ".repeat(10)

        // When
        val command = UpdateScheduleCommentCommand(
            groupId = groupId,
            scheduleId = scheduleId,
            commentId = commentId,
            content = longContent
        )

        // Then
        command.content shouldBe longContent
        command.content.length shouldBe longContent.length
    }

    @Test
    fun `CreateScheduleCommentCommand는 data class 특성을 가진다`() {
        // Given
        val groupId = ObjectId.get()
        val scheduleId = ObjectId.get()
        val content = "테스트"

        val command1 = CreateScheduleCommentCommand(groupId, scheduleId, content)
        val command2 = CreateScheduleCommentCommand(groupId, scheduleId, content)

        // Then
        command1 shouldBe command2
        command1.hashCode() shouldBe command2.hashCode()
    }

    @Test
    fun `UpdateScheduleCommentCommand는 data class 특성을 가진다`() {
        // Given
        val groupId = ObjectId.get()
        val scheduleId = ObjectId.get()
        val commentId = ObjectId.get()
        val content = "테스트"

        val command1 = UpdateScheduleCommentCommand(groupId, scheduleId, commentId, content)
        val command2 = UpdateScheduleCommentCommand(groupId, scheduleId, commentId, content)

        // Then
        command1 shouldBe command2
        command1.hashCode() shouldBe command2.hashCode()
    }

    @Test
    fun `CreateScheduleCommentCommand copy 기능을 사용할 수 있다`() {
        // Given
        val original = CreateScheduleCommentCommand(
            groupId = ObjectId.get(),
            scheduleId = ObjectId.get(),
            content = "원본 내용"
        )

        // When
        val copied = original.copy(content = "수정된 내용")

        // Then
        copied.groupId shouldBe original.groupId
        copied.scheduleId shouldBe original.scheduleId
        copied.content shouldBe "수정된 내용"
    }

    @Test
    fun `UpdateScheduleCommentCommand copy 기능을 사용할 수 있다`() {
        // Given
        val original = UpdateScheduleCommentCommand(
            groupId = ObjectId.get(),
            scheduleId = ObjectId.get(),
            commentId = ObjectId.get(),
            content = "원본 내용"
        )

        // When
        val copied = original.copy(content = "수정된 내용")

        // Then
        copied.groupId shouldBe original.groupId
        copied.scheduleId shouldBe original.scheduleId
        copied.commentId shouldBe original.commentId
        copied.content shouldBe "수정된 내용"
    }
}
