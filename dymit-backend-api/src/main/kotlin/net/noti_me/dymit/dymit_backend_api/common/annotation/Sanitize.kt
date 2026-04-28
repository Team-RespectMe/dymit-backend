package net.noti_me.dymit.dymit_backend_api.common.annotation

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Sanitize {
}