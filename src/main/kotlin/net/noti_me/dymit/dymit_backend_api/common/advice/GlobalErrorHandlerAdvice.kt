package net.noti_me.dymit.dymit_backend_api.common.advice

import net.noti_me.dymit.dymit_backend_api.common.errors.BusinessException
import net.noti_me.dymit.dymit_backend_api.common.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalErrorHandlerAdvice {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(value = [BusinessException::class])
    fun handleBusinessException(exception: BusinessException)
    : ResponseEntity<ErrorResponse> {
        logger.error("BusinessException occurred: ${exception.message}", exception)
        val response = ErrorResponse.of(exception)
        return ResponseEntity(response, HttpStatusCode.valueOf(exception.status))
    }
}