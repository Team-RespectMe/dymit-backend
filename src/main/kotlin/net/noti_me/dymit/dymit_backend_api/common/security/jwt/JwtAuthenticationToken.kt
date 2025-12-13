package net.noti_me.dymit.dymit_backend_api.common.security.jwt

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * JwtAuthenticationToken
 * @param principal 인증 전에는 null, 인증 후에는 MemberInfo 객체
 * @param credentials
 */
class JwtAuthenticationToken(
    private val principal: Any?,
    private val credentials: String? = null,
    authorities: Collection<GrantedAuthority> = emptyList()
) : AbstractAuthenticationToken(authorities) {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        // 인증 완료 상태(principal, authorities 존재)일 때만 authenticated를 true로 설정
        super.setAuthenticated(false)
        if ( principal != null ) {
//            println("JwtAuthenticationToken: 인증 완료 상태로 설정됨")
            super.setAuthenticated(true)
        }
    }

    // 인증 요청용 토큰(미인증 상태)을 생성하는 생성자
    constructor(credentials: String) : this(principal = "", credentials = credentials) {
        super.setAuthenticated(false)
    }

    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        logger.debug("[JwtAuthenticationToken] getAuthorities 호출됨. isAuthenticated: $isAuthenticated")
        if (!isAuthenticated) {
            logger.debug("JwtAuthenticationToken: 미인증 상태이므로 권한 정보가 없습니다.")
            return null
        }
        return (principal as MemberInfo).roles.map {
            SimpleGrantedAuthority(it.name)
        }
    }

    override fun getCredentials(): Any? {
        return credentials
    }

    override fun getPrincipal(): Any? {
        return principal
    }

//    override fun getPrincipal(): Any {
//        return principal
//    }
//
//    override fun getCredentials(): String? {
//        return credentials
//    }
}