package net.noti_me.dymit.dymit_backend_api.common.annotation

import io.swagger.v3.oas.annotations.Parameter

@Parameter(hidden = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class LoginMember(val required: Boolean = true) {

}
