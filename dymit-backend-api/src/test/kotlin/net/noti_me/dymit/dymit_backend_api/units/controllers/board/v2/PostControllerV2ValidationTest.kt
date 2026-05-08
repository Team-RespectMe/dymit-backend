package net.noti_me.dymit.dymit_backend_api.units.controllers.board.v2

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import jakarta.validation.Validator
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.PostCommandRequestV2
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory

internal class PostControllerV2ValidationTest : AnnotationSpec() {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `질문 카테고리 요청은 scheduleId 없이도 유효하다`() {
        val request = PostCommandRequestV2(
            title = "질문 제목",
            content = "질문 내용",
            category = PostCategory.QUESTION,
            scheduleId = null
        )

        val violations = validator.validate(request)
        violations.size shouldBe 0
    }

    @Test
    fun `회고 카테고리에서 scheduleId가 없으면 검증에 실패한다`() {
        val request = PostCommandRequestV2(
            title = "회고 제목",
            content = "회고 내용",
            category = PostCategory.RETROSPECTIVE,
            scheduleId = null
        )

        val violations = validator.validate(request)
        violations.size shouldBe 1
        violations.map { it.message } shouldContain "회고 카테고리 작성 시 scheduleId는 유효한 ObjectId여야 합니다."
    }

    @Test
    fun `회고 카테고리에서 scheduleId가 ObjectId 형식이 아니면 검증에 실패한다`() {
        val request = PostCommandRequestV2(
            title = "회고 제목",
            content = "회고 내용",
            category = PostCategory.RETROSPECTIVE,
            scheduleId = "not-object-id"
        )

        val violations = validator.validate(request)
        violations.size shouldBe 1
        violations.map { it.message } shouldContain "회고 카테고리 작성 시 scheduleId는 유효한 ObjectId여야 합니다."
    }

    @Test
    fun `제목이 비어 있으면 검증에 실패한다`() {
        val request = PostCommandRequestV2(
            title = "",
            content = "본문",
            category = PostCategory.QUESTION,
            scheduleId = null
        )

        val violations = validator.validate(request)
        violations.map { it.message } shouldContain "제목은 비어 있을 수 없습니다."
    }
}
