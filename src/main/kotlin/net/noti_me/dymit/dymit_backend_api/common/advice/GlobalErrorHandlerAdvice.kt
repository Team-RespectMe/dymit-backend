package net.noti_me.dymit.dymit_backend_api.common.advice

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import jakarta.validation.ConstraintViolationException
import net.noti_me.dymit.dymit_backend_api.common.errors.BusinessException
import net.noti_me.dymit.dymit_backend_api.common.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestValueException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException

@RestControllerAdvice
class GlobalErrorHandlerAdvice {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    fun handleHttpMessageNotReadableException(exception: HttpMessageNotReadableException)
    : ResponseEntity<ErrorResponse> {
        logger.error("HttpMessageNotReadableException occurred: ${exception.message}", exception)
        val response = if ( JsonMappingException::class.java.isInstance(exception.cause)  ) {
            logger.error("JsonMappingException occurred: ${exception.cause}")
            ErrorResponse.of(exception.cause as JsonMappingException)
        } else {
            ErrorResponse.of(status = HttpStatus.BAD_REQUEST, code = "BAD_REQUEST", message = "잘못된 요청 형식")
        }

        return ResponseEntity(response, HttpStatusCode.valueOf(400)) // Bad Request
    }

    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun handleConstraintViloationException(exception: ConstraintViolationException)
    :ResponseEntity<ErrorResponse> {
        logger.error("ConstraintViolationException occurred: ${exception.message}", exception)
        val response = ErrorResponse.of(status = HttpStatus.BAD_REQUEST, code="BAD_REQUEST", message = "입력 제한 위반", exception.constraintViolations)
        return ResponseEntity(response, HttpStatusCode.valueOf(400))
//        return ResponseEntity(response, HttpStatusCode.valueOf(400)) // Bad Request
    }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException)
    : ResponseEntity<ErrorResponse> {
        logger.error("MethodArgumentNotValidException occurred: ${exception.message}", exception)
        val response = ErrorResponse.of(status = HttpStatus.BAD_REQUEST, code = "BAD_REQUEST", message = "유효성 검사 실패", exception.bindingResult)
        return ResponseEntity(response, HttpStatusCode.valueOf(400)) // Bad Request
    }

    @ExceptionHandler(value = [BusinessException::class])
    fun handleBusinessException(exception: BusinessException)
    : ResponseEntity<ErrorResponse> {
        logger.error("BusinessException occurred: ${exception.message}", exception)
        val response = ErrorResponse.of(exception)
        return ResponseEntity(response, HttpStatusCode.valueOf(exception.status))
    }

    @ExceptionHandler(value = [HttpRequestMethodNotSupportedException::class])
    fun handleHttpRequestMethodNotSupportedException(exception: HttpRequestMethodNotSupportedException)
    : ResponseEntity<ErrorResponse> {
//        logger.error("HttpRequestMethodNotSupportedException occurred: ${exception.message}", exception)
        val response = ErrorResponse.of(status = HttpStatus.METHOD_NOT_ALLOWED, code = "METHOD_NOT_ALLOWED", message = "지원하지 않는 HTTP 메소드")
        return ResponseEntity(response, HttpStatusCode.valueOf(405)) // Method Not Allowed
    }

    @ExceptionHandler
    fun handleException(exception: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception occurred: ${exception.message}", exception)
        val response = ErrorResponse.of(status = HttpStatus.INTERNAL_SERVER_ERROR, code = "INTERNAL_SERVER_ERROR", message = exception.message)
        return ResponseEntity(response, HttpStatusCode.valueOf(500)) // Internal Server Error
    }
}