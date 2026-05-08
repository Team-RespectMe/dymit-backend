package net.noti_me.dymit.dymit_backend_api.units.controllers.files

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import jakarta.validation.Validator
import net.noti_me.dymit.dymit_backend_api.controllers.files.dto.FileUploadRequest

internal class FileUploadRequestValidationTest : AnnotationSpec() {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `file 값이 없으면 검증에 실패한다`() {
        val request = FileUploadRequest(file = null)

        val violations = validator.validate(request)

        violations.size shouldBe 1
        violations.map { it.message } shouldContain "업로드할 파일은 필수입니다."
    }
}
