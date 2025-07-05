package net.noti_me.dymit.dymit_backend_api.common.response

import com.fasterxml.jackson.databind.JsonMappingException
import jakarta.validation.ConstraintViolation
import net.noti_me.dymit.dymit_backend_api.common.errors.BusinessException
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException


class ErrorResponse(
    val traceId: String? = MDC.get("traceId"),
    val status: Int = 400,
    val code: String? = null,
    val message: String? = null,
    var errors: List<FieldError> = listOf()
) {

    companion object {
//
//        fun of(exception: HttpMessageNotReadableException) {
//            return ErrorResponse.of(
//                status = HttpStatus.BAD_REQUEST,
//                code = "BAD_REQUEST",
//                message = "잘못된 요청 입니다. 요청 body를 확인해주세요.",
//                errors = FieldError.of(exception)
//            )
//        }

        fun of(exception: JsonMappingException): ErrorResponse {
            return ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                code = "BAD_REQUEST",
                message = "잘못된 요청 형식입니다.",
                errors = FieldError.of(exception.path)
            )
        }

        fun of(exception: BusinessException)
                = ErrorResponse(code = exception.code, message = exception.message, status = exception.status)

        fun of(status: HttpStatus, code: String?, message: String?, bindingResult: BindingResult)
                = ErrorResponse(code = code, message = message, status = status.value(), errors =FieldError.of(bindingResult))

        fun of(status: HttpStatus, code: String?, message: String?)
                = ErrorResponse(code = code, message = message, status = status.value())

        fun of(status: HttpStatus, code: String?, message: String?, violations: Set<ConstraintViolation<*>>)
                = ErrorResponse(code = code, message = message, status = status.value(), errors =FieldError.of(violations))

        fun of(status: HttpStatus, code: String?, message: String?, errors: List<FieldError>)
                = ErrorResponse(code = code, message = message, status = status.value(), errors =errors)

        fun of(e: MethodArgumentTypeMismatchException): ErrorResponse {
            val value = e.value?.toString() ?: ""
            val errors = FieldError.of(e.name, value, e.errorCode);
            return ErrorResponse(status=400, code="입력 값의 자료형이 잘못되었습니다.", message = e.value?.toString(), errors=errors);
        }
    }


    class FieldError(
        val field: String,
        val value: String,
        val reason: String
    ) {

        companion object {

            fun of(path: List<JsonMappingException.Reference>): List<FieldError> {
                return path.map { ref ->
                    FieldError(
                        field = ref.fieldName ?: "unknown",
                        value = "",
                        reason = ref.description ?: "No description"
                    )
                }
            }

            fun of(field: String, value: String, reason: String): List<FieldError> {
                return listOf(FieldError(field, value, reason))
            }

            fun of(bindingResult: BindingResult): List<FieldError> {
                val fieldErrors: List<org.springframework.validation.FieldError> = bindingResult.fieldErrors

                return fieldErrors.stream().map {
                    FieldError(
                        it.field,
                        it.rejectedValue?.toString() ?: "",
                        it.defaultMessage ?: ""
                    )
                }.toList()
            }

            fun of(constraintViolations: Set<ConstraintViolation<*>>): List<FieldError> {
                return constraintViolations.toList()
                    .map { error ->
                        val invalidValue = error.invalidValue?.toString() ?: ""
                        val index = error.propertyPath.toString().indexOf(".")
//                        val propertyPath = error.propertyPath.toString().substring(index + 1)
                        val propertyPath = error.propertyPath.toString().split(".").last()
                        FieldError(propertyPath, invalidValue, error.message ?: "Validation error")
                    }
            }
        }
    }
}