package net.noti_me.dymit.dymit_backend_api.common.annotation

import net.noti_me.dymit.dymit_backend_api.common.errors.UnauthorizedException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class LoginMemberResolver : HandlerMethodArgumentResolver{

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(LoginMember::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val required = parameter.getParameterAnnotation(LoginMember::class.java)!!
            .required
        val principal: MemberInfo? = SecurityContextHolder.getContext().authentication.principal as MemberInfo
        return if (required) {
            principal ?: throw UnauthorizedException("인증 정보가 필요합니다.")
        } else {
            principal
        }
    }
}