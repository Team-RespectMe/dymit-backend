package net.noti_me.dymit.dymit_backend_api.common.constraints.nickname

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [NicknameValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Nickname(
    val message: String = "닉네임은 한글, 영문, 숫자 그리고 특수 문자 _을 허용합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
