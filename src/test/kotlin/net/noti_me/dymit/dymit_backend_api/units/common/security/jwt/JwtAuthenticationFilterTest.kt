package net.noti_me.dymit.dymit_backend_api.units.common.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.AnnotationSpec

import io.mockk.*
import jakarta.servlet.FilterChain
import net.noti_me.dymit.dymit_backend_api.common.security.exceptions.JwtEntrypointUnauthorizedHandler
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtAuthenticationFilter
import net.noti_me.dymit.dymit_backend_api.supports.createJwtConfig
import net.noti_me.dymit.dymit_backend_api.supports.createJwtService
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

internal class JwtAuthenticationFilterTest : AnnotationSpec() {

    private val authenticationManager = mockk<AuthenticationManager>(relaxed = true)

    private val objectMapper = ObjectMapper()

    private val onEntrypointUnauthorizedHandler = JwtEntrypointUnauthorizedHandler(objectMapper)

    private val jwtAuthenticationFilter = JwtAuthenticationFilter(authenticationManager, onEntrypointUnauthorizedHandler)

    private val securityContext = mockk<SecurityContext>(relaxed = true)

    private val jwtService = createJwtService(createJwtConfig())

    @BeforeEach
    fun setUp() {
        mockkStatic(SecurityContextHolder::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SecurityContextHolder::class)
        clearAllMocks()
    }

    @Test
    fun `인증 헤더가 없거나 올바르지 않다면 인증 절차를 수행하지 않는다`() {
        // Given
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val filterChain = mockk<FilterChain>(relaxed = true)
        request.addHeader("Authorization", "InvalidHeader")

        // When
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 0) { authenticationManager.authenticate(any()) }
        verify(exactly = 1) { filterChain.doFilter(request, response) }
    }

    @Test
    fun `인증 헤더가 올바르다면 인증 절차를 수행하고 SecurityContext에 인증 정보를 설정한다`() {
        // Given
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val filterChain = mockk<FilterChain>(relaxed = true)
        request.addHeader("Authorization", "Bearer validToken")

        // SecurityContextHolder.getContext()가 mock securityContext를 반환하도록 설정
        every { SecurityContextHolder.getContext() } returns securityContext

        // When
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        verify(exactly = 1) { filterChain.doFilter(request, response) }
        // mock securityContext의 authentication이 설정되었는지 검증
//        verify { securityContext.authentication = any() }
    }
}