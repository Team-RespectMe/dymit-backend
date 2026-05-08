package net.noti_me.dymit.dymit_backend_api.units.controllers.board.v2

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import jakarta.validation.Validator
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.BoardCategoryPolicyRequestV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2.UpdateBoardCategoryPoliciesRequestV2
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory

internal class BoardCategoryControllerV2ValidationTest : AnnotationSpec() {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `카테고리 정책 목록이 비어 있으면 검증에 실패한다`() {
        val request = UpdateBoardCategoryPoliciesRequestV2(policies = emptyList())

        val violations = validator.validate(request)
        violations.size shouldBe 1
        violations.map { it.message } shouldContain "카테고리 정책은 비어 있을 수 없습니다."
    }

    @Test
    fun `카테고리 정책 목록이 있으면 검증에 성공한다`() {
        val request = UpdateBoardCategoryPoliciesRequestV2(
            policies = listOf(
                BoardCategoryPolicyRequestV2(
                    category = PostCategory.NOTICE,
                    enabled = true,
                    writePolicy = BoardCategoryWritePolicy.GROUP_ADMIN_ONLY
                )
            )
        )

        val violations = validator.validate(request)
        violations.size shouldBe 0
    }
}
