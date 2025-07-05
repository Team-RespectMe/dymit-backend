package net.noti_me.dymit.dymit_backend_api.e2e.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Order
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.TestCaseOrder
import io.kotest.extensions.spring.SpringExtension
import net.noti_me.dymit.dymit_backend_api.controllers.AuthController
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcLoginRequest
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.RefreshTokenSubmitRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@Order(2)
class AuthControllerTest(
    private val mockMvc: MockMvc,
    private val om: ObjectMapper,
    @Value("\${test.id-token.google}")
    private val googleTestIdToken: String
): AnnotationSpec() {

    private var accessToken: String = ""

    private var refreshToken: String = ""

    override fun extensions(): List<Extension> {
        return listOf(SpringExtension)
    }

    override fun testCaseOrder(): TestCaseOrder? {
        return TestCaseOrder.Sequential
    }

    @Test
    fun `001 Google Login Test`() {
        val request = OidcLoginRequest(
            idToken = googleTestIdToken,
            provider = OidcProvider.GOOGLE
        )

        val result = mockMvc.post("/api/v1/auth/oidc") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.data.accessToken") { isNotEmpty() }
            jsonPath("$.data.refreshToken") { isNotEmpty() }
        }.andReturn()

        accessToken = JsonPath.read(result.response.contentAsString, "$.data.accessToken")
        refreshToken = JsonPath.read(result.response.contentAsString, "$.data.refreshToken")
    }

    @Test
    fun `002 정상 Refresh token을 통한 Access token 재발급 테스트`(){
        val request = RefreshTokenSubmitRequest(
            refreshToken = refreshToken
        )

        mockMvc.post("/api/v1/auth/jwt/reissue") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.data.accessToken") { isNotEmpty() }
        }
    }

    @Test
    fun `003 잘못된 Refresh token을 통한 Access token 재발급 테스트`() {
        val invalidRefreshToken = refreshToken.substring(0, refreshToken.length - 1)

        val request = RefreshTokenSubmitRequest(
            invalidRefreshToken
        )
        mockMvc.post("/api/v1/auth/jwt/reissue") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(request)
        }.andExpect {
            status { isUnauthorized() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }
}