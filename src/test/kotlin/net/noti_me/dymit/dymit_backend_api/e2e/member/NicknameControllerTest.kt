package net.noti_me.dymit.dymit_backend_api.e2e.member

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.AnnotationSpec
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@io.kotest.core.spec.Order(2)
class NicknameControllerTest(
    private val mockMvc: MockMvc,
//    private val om : ObjectMapper
): AnnotationSpec()  {

    override fun extensions() = listOf(io.kotest.extensions.spring.SpringExtension)

    @Test
    fun `중복 닉네임을 유효성 검사하면 CONFLICT 가 반환된다`() {
        // Given
        val duplicatedNickname = "new nickname"

        // When
        val mockRequest = mockMvc.get("/api/v1/nicknames/validate") {
            param("nickname", duplicatedNickname)
        }

        // Then
        mockRequest.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `닉네임 규칙을 위반하면 BAD_REQUEST 가 반환된다`() {
        // Given
        val invalidNickname = "invalid nickname!"

        // When
        val mockRequest = mockMvc.get("/api/v1/nicknames/validate") {
            param("nickname", invalidNickname)
        }

        // Then
        mockRequest.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `유효한 닉네임을 검사하면 OK 가 반환된다`() {
        // Given
        val validNickname = "validNickname"

        // When
        val mockRequest = mockMvc.get("/api/v1/nicknames/validate") {
            param("nickname", validNickname)
        }

        // Then
        mockRequest.andExpect {
            status { isOk() }
        }
    }
}