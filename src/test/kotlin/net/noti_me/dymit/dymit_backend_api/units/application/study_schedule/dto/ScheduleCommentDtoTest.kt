package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule.dto

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ScheduleCommentDto
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.WriterVo
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * ScheduleCommentDto 테스트 클래스
 * 스케줄 댓글 DTO의 생성자와 팩토리 메서드를 테스트합니다.
 */
class ScheduleCommentDtoTest : AnnotationSpec() {

    /**
     * 테스트용 Writer 객체를 생성하는 헬퍼 함수
     */
    private fun createTestWriter(
        id: ObjectId = ObjectId.get(),
        nickname: String = "테스트작성자",
        imageUrl: String = "profile.jpg"
    ): Writer {
        return Writer(
            id = id,
            nickname = nickname,
            image = ProfileImageVo(
                type = "preset",
                url = imageUrl
            )
        )
    }

    /**
     * 테스트용 ScheduleComment 엔티티를 생성하는 헬퍼 함수
     */
    private fun createTestScheduleComment(
        id: ObjectId = ObjectId.get(),
        scheduleId: ObjectId = ObjectId.get(),
        writer: Writer = createTestWriter(),
        content: String = "테스트 댓글 내용"
    ): ScheduleComment {
        return ScheduleComment(
            id = id,
            scheduleId = scheduleId,
            writer = writer,
            content = content
        )
    }

    /**
     * 테스트용 WriterVo 객체를 생성하는 헬퍼 함수
     */
    private fun createTestWriterVo(
        memberId: String = "writer789",
        nickname: String = "작성자",
        imageUrl: String = "writer.jpg"
    ): WriterVo {
        return WriterVo(
            memberId = memberId,
            nickname = nickname,
            image = ProfileImageVo(
                type = "preset",
                url = imageUrl
            )
        )
    }

    @Test
    fun `생성자로 객체를 정상적으로 생성할 수 있다`() {
        // Given
        val id = "comment123"
        val scheduleId = "schedule456"
        val writer = createTestWriterVo()
        val createdAt = LocalDateTime.of(2024, 12, 25, 15, 0)
        val content = "댓글 내용"

        // When
        val dto = ScheduleCommentDto(
            id = id,
            scheduleId = scheduleId,
            writer = writer,
            createdAt = createdAt,
            content = content
        )

        // Then
        dto.id shouldBe id
        dto.scheduleId shouldBe scheduleId
        dto.writer shouldBe writer
        dto.createdAt shouldBe createdAt
        dto.content shouldBe content
    }

    @Test
    fun `from 메서드로 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = createTestScheduleComment()

        // When
        val dto = ScheduleCommentDto.from(entity)

        // Then
        dto.id shouldBe entity.id.toString()
        dto.scheduleId shouldBe entity.scheduleId.toString()
        dto.writer.memberId shouldBe entity.writer.id.toString()
        dto.writer.nickname shouldBe entity.writer.nickname
        dto.writer.image shouldBe entity.writer.image
        dto.createdAt shouldNotBe null
        dto.content shouldBe entity.content
    }

    @Test
    fun `빈 내용을 가진 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = createTestScheduleComment(content = "")

        // When
        val dto = ScheduleCommentDto.from(entity)

        // Then
        dto.content shouldBe ""
    }

    @Test
    fun `긴 내용을 가진 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val longContent = "매우 긴 댓글 내용입니다. ".repeat(50)
        val entity = createTestScheduleComment(content = longContent)

        // When
        val dto = ScheduleCommentDto.from(entity)

        // Then
        dto.content shouldBe longContent
        dto.content.length shouldBe longContent.length
    }

    @Test
    fun `다양한 ObjectId를 가진 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val commentId = ObjectId.get()
        val scheduleId = ObjectId.get()
        val writerId = ObjectId.get()

        val writer = createTestWriter(
            id = writerId,
            nickname = "특별한작성자",
            imageUrl = "special.jpg"
        )

        val entity = createTestScheduleComment(
            id = commentId,
            scheduleId = scheduleId,
            writer = writer,
            content = "특별한 댓글"
        )

        // When
        val dto = ScheduleCommentDto.from(entity)

        // Then
        dto.id shouldBe commentId.toString()
        dto.scheduleId shouldBe scheduleId.toString()
        dto.writer.memberId shouldBe writerId.toString()
    }
}
