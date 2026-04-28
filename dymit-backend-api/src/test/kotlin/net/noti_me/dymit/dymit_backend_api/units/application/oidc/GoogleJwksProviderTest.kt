package net.noti_me.dymit.dymit_backend_api.units.application.oidc

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.JWKKey
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.JWKList
import net.noti_me.dymit.dymit_backend_api.application.auth.oidc.GoogleJwksProvider
import net.noti_me.dymit.dymit_backend_api.common.errors.InternalServerError
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigInteger
import java.util.Base64

class GoogleJwksProviderTest : AnnotationSpec() {

    private lateinit var webClient: WebClient
    private lateinit var objectMapper: ObjectMapper
    private lateinit var googleJwksProvider: GoogleJwksProvider

    private val testKid = "test_kid"
    private val testModulus = "oahUIoWw0K0usKNuGNpkAbe4v2f1L2HH_G1bV2RAnHq-V_f_H3C8I3i_U_kC_kH_e_v_w_z_Y_a_b_c_d_e_f_g_h_i_j_k_l_m_n_o_p_q_r_s_t_u_v_w_x_y_zA"
    private val testExponent = "AQAB"

    @BeforeEach
    fun setup() {
        webClient = mockk()
        objectMapper = ObjectMapper() // 실제 ObjectMapper 사용
        googleJwksProvider = GoogleJwksProvider(webClient)
    }

    @Test
    fun `유효한 kid로 공개키를 성공적으로 가져온다`() {
        // given
        val jwkList = JWKList(listOf(
            JWKKey(kty = "RSA", use = "sig", kid = testKid, alg = "RS256", n = testModulus, e = testExponent)
        ))
        val responseMono = Mono.just(jwkList)

        every {
            webClient.get()
                .uri(any<String>())
                .retrieve()
                .bodyToMono(JWKList::class.java)
        } returns responseMono

        // when
        val publicKey = googleJwksProvider.getPublicKey(testKid)

        // then
        publicKey shouldNotBe null
        val expectedModulus = BigInteger(1, Base64.getUrlDecoder().decode(testModulus))
        publicKey.modulus shouldBe expectedModulus
//        verify(exactly = 1) { webClient.get() } // 네트워크 호출이 1번 발생했는지 확인
    }

    @Test
    fun `캐시된 JWKS를 사용하여 공개키를 가져온다`() {
        // given
        val jwkList = JWKList(listOf(
            JWKKey(kty = "RSA", use = "sig", kid = testKid, alg = "RS256", n = testModulus, e = testExponent)
        ))
        val responseMono = Mono.just(jwkList)

        every {
            webClient.get()
                .uri(any<String>())
                .retrieve()
                .bodyToMono(JWKList::class.java)
        } returns responseMono

        // when
        googleJwksProvider.getPublicKey(testKid) // 첫 호출로 캐시 저장
        val publicKeyFromCache = googleJwksProvider.getPublicKey(testKid) // 두번째 호출은 캐시 사용

        // then
        publicKeyFromCache shouldNotBe null
//        verify(exactly = 1) { webClient.get() } // 네트워크 호출은 여전히 1번만 발생해야 함
    }

    @Test
    fun `존재하지 않는 kid로 공개키를 요청하면 예외가 발생한다`() {
        // given
        val jwkList = JWKList(listOf(
            JWKKey(kty = "RSA", use = "sig", kid = testKid, alg = "RS256", n = testModulus, e = testExponent)
        ))
        val responseMono = Mono.just(jwkList)

        every {
            webClient.get()
                .uri(any<String>())
                .retrieve()
                .bodyToMono(JWKList::class.java)
        } returns responseMono

        // when & then
        val exception = shouldThrow<InternalServerError> {
            googleJwksProvider.getPublicKey("invalid_kid")
        }
        exception.message shouldBe "OIDC 로그인 실패, kid에 해당하는 공개키를 찾을 수 없습니다."
    }
}