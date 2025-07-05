package net.noti_me.dymit.dymit_backend_api.e2e.member

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import io.kotest.core.spec.Order
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.TestCaseOrder
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberNicknameUpdateRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@Order(1)
class MemberControllerTest(
    private val mockMvc: MockMvc,
    private val om: ObjectMapper,
    @Value("\${test.id-token.google}")
    private val googleTestIdToken: String
) : AnnotationSpec() {

    private var accessToken: String = ""

    private var memberId: String = ""

    private val GOOGLE_JOIN_FORM = MemberCreateRequest(
        nickname = "google nickname",
        oidcProvider = OidcProvider.GOOGLE,
        idToken = googleTestIdToken
    )

    override fun testCaseOrder(): TestCaseOrder? {
        return TestCaseOrder.Sequential
    }

    @Test
    fun `001 Google 회원가입 테스트`() {
        // Given
        val form = GOOGLE_JOIN_FORM

        // When
        val result = mockMvc.post("/api/v1/members") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(form)
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.data.memberId") {  isNotEmpty() }
            jsonPath("$.data.accessToken") { isNotEmpty() }
            jsonPath("$.data.refreshToken") { isNotEmpty() }
        }.andReturn()

        val responseBody = result.response.contentAsString
        accessToken = JsonPath.read(responseBody, "$.data.accessToken")
        memberId = JsonPath.read(responseBody, "$.data.memberId")
    }

    @Test
    fun `002 회원 단건 조회 테스트`() {

        mockMvc.get("/api/v1/members/${memberId}") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${accessToken}")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.data.id") { value(memberId) }
            jsonPath("$.data.nickname") { value(GOOGLE_JOIN_FORM.nickname) }
            jsonPath("$.data.oidcIdentities[0].provider") { value(GOOGLE_JOIN_FORM.oidcProvider.name) }
        }
    }

    @Test
    fun `003 중복되지 않는 올바른 형식의 닉네임을 통한 닉네임 변경 요청 테스트`() {

        // Given
        val newNickname = "new nickname"
        val nicknameChangeRequest = MemberNicknameUpdateRequest(
            nickname = newNickname
        )

        //When
        val request = mockMvc.patch("/api/v1/members/${memberId}/nickname") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${accessToken}")
            content = om.writeValueAsString(nicknameChangeRequest)
        }

        // Then
        request.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.data.nickname") { value(newNickname) }
        }
    }
}